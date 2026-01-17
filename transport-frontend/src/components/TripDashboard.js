import React, { useState, useEffect, useRef } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMap } from 'react-leaflet';
import L from 'leaflet';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import api from '../api/api';

// --- 1. FIX ICÔNES PAR DÉFAUT (Leaflet + React bug fix) ---
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

let DefaultIcon = L.icon({
    iconUrl: markerIcon,
    shadowUrl: markerShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41]
});
L.Marker.prototype.options.icon = DefaultIcon;

// --- CONFIGURATION DES ICÔNES PERSONNALISÉES ---
const busIcon = new L.Icon({
    iconUrl: 'https://cdn-icons-png.flaticon.com/512/3448/3448339.png',
    iconSize: [45, 45],
    iconAnchor: [22, 45],
});

const parentIcon = (status) => {
    let url = 'https://cdn-icons-png.flaticon.com/512/25/25694.png';
    if (status === 'COMPLETED') url = 'https://cdn-icons-png.flaticon.com/512/190/190411.png';
    if (status === 'LATE_BOARDING') url = 'https://cdn-icons-png.flaticon.com/512/595/595067.png';

    return new L.Icon({
        iconUrl: url,
        iconSize: [30, 30],
        iconAnchor: [15, 30],
    });
};

// --- STYLES ---
const sidebarStyle = {
    width: '300px',
    minWidth: '300px',
    backgroundColor: '#2c3e50',
    padding: '20px',
    color: 'white',
    display: 'flex',
    flexDirection: 'column',
    zIndex: 1000
};

const statusCardStyle = {
    background: '#34495e',
    padding: '15px',
    borderRadius: '8px',
    marginBottom: '20px'
};

const btnStartStyle = {
    backgroundColor: '#27ae60',
    color: 'white',
    border: 'none',
    padding: '12px',
    borderRadius: '5px',
    fontWeight: 'bold',
    cursor: 'pointer',
    marginBottom: '10px'
};

const btnResetStyle = {
    backgroundColor: '#e67e22',
    color: 'white',
    border: 'none',
    padding: '12px',
    borderRadius: '5px',
    fontWeight: 'bold',
    cursor: 'pointer',
    marginBottom: '20px'
};

const arrivalBoxStyle = {
    padding: '15px',
    borderRadius: '8px',
    textAlign: 'center',
    marginBottom: '20px'
};

const alertContainerStyle = {
    flex: 1,
    overflowY: 'auto'
};

const alertItemStyle = {
    backgroundColor: '#c0392b',
    padding: '10px',
    borderRadius: '4px',
    marginBottom: '8px',
    fontSize: '0.85em'
};

// --- 2. COMPOSANT POUR RÉPARER LA TAILLE DE LA CARTE ---
function MapManager({ position }) {
    const map = useMap();

    useEffect(() => {
        // Force Leaflet à recalculer sa taille (fixe les carrés gris)
        setTimeout(() => {
            map.invalidateSize();
        }, 400);
    }, [map]);

    useEffect(() => {
        if (position && position.latitude) {
            // Optionnel: décommenter pour suivre le bus automatiquement
            // map.setView([position.latitude, position.longitude], map.getZoom());
        }
    }, [position, map]);

    return null;
}

export default function TripDashboard() {
    const [busPos, setBusPos] = useState({
        latitude: 33.595,
        longitude: -7.600,
        speed: 0,
        status: 'INITIALIZING'
    });

    const [stops, setStops] = useState([]);
    const [alerts, setAlerts] = useState([]);
    const [currentStop, setCurrentStop] = useState(null);
    const [waitTime, setWaitTime] = useState(0);
    const stompClientRef = useRef(null);

    useEffect(() => {
        fetchStops();
        const socket = new SockJS('http://localhost:8081/ws');
        const client = new Client({
            webSocketFactory: () => socket,
            reconnectDelay: 5000,
            onConnect: () => {
                client.subscribe('/topic/bus-location', (msg) => {
                    const data = JSON.parse(msg.body);
                    setBusPos(data);
                    if (data.status === 'ARRIVED' || data.status === 'EN_ROUTE') {
                        fetchStops();
                    }
                });
                client.subscribe('/topic/alerts', (msg) => {
                    setAlerts(prev => [msg.body, ...prev]);
                });
            }
        });
        client.activate();
        stompClientRef.current = client;
        return () => client.deactivate();
    }, []);

    useEffect(() => {
        let interval;
        if (currentStop && (currentStop.status === 'ARRIVED' || currentStop.status === 'LATE_BOARDING')) {
            const arrivalTime = new Date(currentStop.actualArrival).getTime();
            interval = setInterval(() => {
                const now = new Date().getTime();
                setWaitTime(Math.floor((now - arrivalTime) / 1000));
            }, 1000);
        } else {
            setWaitTime(0);
        }
        return () => clearInterval(interval);
    }, [currentStop]);

    const fetchStops = async () => {
        try {
            const res = await api.get("/trip-stops/trip/1");
            setStops(res.data);
            const active = res.data.find(s => s.status === 'ARRIVED' || s.status === 'LATE_BOARDING');
            setCurrentStop(active || null);
        } catch (err) {
            console.error("Erreur API stops:", err);
        }
    };

    const handleStartSimulation = () => {
        api.post("/tracking/start-simulation?tripId=1").catch(console.error);
    };

    const handleResetDatabase = async () => {
        if (window.confirm("Voulez-vous réinitialiser tous les arrêts pour un nouveau test ?")) {
            try {
                await api.post("/trip-stops/reset-demo");
                alert("Base de données réinitialisée !");
                window.location.reload(); // Recharge pour voir les changements
            } catch (err) {
                console.error("Erreur reset:", err);
                alert("Erreur lors du reset");
            }
        }
    };

    const formatTime = (seconds) => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}:${secs.toString().padStart(2, '0')}`;
    };

    return (
        <div style={{ display: 'flex', height: '100vh', width: '100vw', backgroundColor: '#f4f7f6', overflow: 'hidden' }}>
            {/* SIDEBAR */}
            <div style={sidebarStyle}>
                <h2 style={{ color: '#f1c40f', marginBottom: '20px' }}>🚍 Admin Panel</h2>

                <div style={statusCardStyle}>
                    <p>Statut: <span style={{color: '#f1c40f'}}>{busPos.status}</span></p>
                    <p>Vitesse: <strong>{Math.round(busPos.speed)} km/h</strong></p>
                </div>

                <button onClick={handleStartSimulation} style={btnStartStyle}>
                    DÉMARRER LA SIMULATION
                </button>

                <button onClick={handleResetDatabase} style={btnResetStyle}>
                    🔄 RESET DATABASE
                </button>

                {currentStop && (
                    <div style={{
                        ...arrivalBoxStyle,
                        backgroundColor: waitTime >= 300 ? '#e74c3c' : '#f1c40f',
                        color: waitTime >= 300 ? 'white' : '#2c3e50'
                    }}>
                        <p>📍 Arrêt : <strong>{currentStop.parent.lastName}</strong></p>
                        <div style={{ fontSize: '2em', fontWeight: 'bold' }}>{formatTime(waitTime)}</div>
                        {waitTime >= 300 && <small>⚠️ PÉNALITÉ APPLIQUÉE</small>}
                    </div>
                )}

                <div style={alertContainerStyle}>
                    <h4>🔔 Alertes</h4>
                    {alerts.map((a, i) => <div key={i} style={alertItemStyle}>{a}</div>)}
                </div>
            </div>

            {/* MAP CONTAINER */}
            <div style={{ flex: 1, position: 'relative', height: '100%' }}>
                <MapContainer
                    center={[33.588, -7.611]}
                    zoom={14}
                    style={{ height: '100%', width: '100%' }}
                >
                    <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />

                    <MapManager position={busPos} />

                    {stops.length > 0 && (
                        <Polyline
                            positions={stops.map(s => [s.parent.latitude, s.parent.longitude])}
                            color="#2c3e50"
                            weight={3}
                            dashArray="10, 10"
                            opacity={0.5}
                        />
                    )}

                    <Marker position={[busPos.latitude, busPos.longitude]} icon={busIcon}>
                        <Popup>Bus Scolaire - ID #1</Popup>
                    </Marker>

                    {stops.map(stop => (
                        <Marker
                            key={stop.id}
                            position={[stop.parent.latitude, stop.parent.longitude]}
                            icon={parentIcon(stop.status)}
                        >
                            <Popup>
                                <strong>{stop.parent.firstName} {stop.parent.lastName}</strong><br/>
                                Statut: {stop.status}
                            </Popup>
                        </Marker>
                    ))}
                </MapContainer>
            </div>
        </div>
    );
}
