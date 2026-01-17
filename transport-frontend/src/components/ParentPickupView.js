import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import api from '../api/api';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

// --- CONFIGURATION DES ICÔNES ---
const busIcon = new L.Icon({
    iconUrl: 'https://cdn-icons-png.flaticon.com/512/3448/3448339.png',
    iconSize: [40, 40],
    iconAnchor: [20, 40],
});

const homeIcon = new L.Icon({
    iconUrl: 'https://cdn-icons-png.flaticon.com/512/25/25694.png',
    iconSize: [35, 35],
    iconAnchor: [17, 35],
});

// Composant pour forcer la carte à suivre le bus
function MapFollower({ position }) {
    const map = useMap();
    useEffect(() => {
        if (position) {
            map.panTo([position.latitude, position.longitude]);
        }
    }, [position, map]);
    return null;
}

export default function ParentPickupView({ parentId }) {
    const [myStop, setMyStop] = useState(null);
    const [busPos, setBusPos] = useState({ latitude: 33.588, longitude: -7.611 });
    const [waitTime, setWaitTime] = useState(0);

    useEffect(() => {
        fetchMyStatus();

        // --- WEBSOCKETS ---
        const socket = new SockJS('http://localhost:8081/ws');
        const client = new Client({
            webSocketFactory: () => socket,
            onConnect: () => {
                // 1. Suivre la position du bus en temps réel
                client.subscribe('/topic/bus-location', (msg) => {
                    const data = JSON.parse(msg.body);
                    setBusPos(data);
                });

                // 2. Écouter les alertes ou changements de statut
                client.subscribe(`/topic/parent/${parentId}`, (msg) => {
                    const data = JSON.parse(msg.body);
                    setMyStop(data);
                });
            }
        });
        client.activate();
        return () => client.deactivate();
    }, [parentId]);

    // Timer de présence du bus
    useEffect(() => {
        let interval;
        if (myStop && (myStop.status === 'ARRIVED' || myStop.status === 'LATE_BOARDING')) {
            const arrivalTime = new Date(myStop.actualArrival).getTime();
            interval = setInterval(() => {
                setWaitTime(Math.floor((new Date().getTime() - arrivalTime) / 1000));
            }, 1000);
        }
        return () => clearInterval(interval);
    }, [myStop]);

    const fetchMyStatus = async () => {
        try {
            const res = await api.get(`/trip-stops/parent/${parentId}`);
            setMyStop(res.data);
        } catch (err) {
            console.error("Aucun arrêt actif trouvé pour ce parent.");
        }
    };
    const handleRunExportBatch = async () => {
        try {
            // On appelle ton endpoint Batch
            const response = await api.post('/batch/run-export');
            alert("Succès : " + response.data);
        } catch (err) {
            console.error(err);
            alert("Erreur : Le batch n'a pas pu démarrer. Vérifie la console Java.");
        }
    };

    const handleConfirmPickup = async () => {
        try {
            await api.put(`/trip-stops/${myStop.id}/complete`);
            setMyStop({ ...myStop, status: 'COMPLETED' });
        } catch (err) {
            alert("Erreur confirmation");
        }
    };

    if (!myStop) return <div style={containerStyle}>Chargement de votre trajet...</div>;

    return (
        <div style={containerStyle}>
            <div style={sidebarStyle}>
                <h2 style={{color: '#2c3e50'}}>🚍 Parent : {myStop.parent.lastName}</h2>
                <hr/>

                {/* LOGIQUE D'AFFICHAGE SELON LE STATUT */}
                {myStop.status === 'SCHEDULED' && (
                    <div style={infoBox}>
                        <p>🚌 Le bus est en route.</p>
                        <small>Gardez un œil sur la carte !</small>
                    </div>
                )}

                {(myStop.status === 'ARRIVED' || myStop.status === 'LATE_BOARDING') && (
                    <div style={arrivalBox}>
                        <h3 style={{margin: 0}}>📍 LE BUS EST LÀ !</h3>
                        <div style={timerStyle}>
                            {Math.floor(waitTime / 60)}:{String(waitTime % 60).padStart(2, '0')}
                        </div>
                        <button onClick={handleConfirmPickup} style={confirmBtnStyle}>
                            ✅ J'AI RÉCUPÉRÉ MON ENFANT
                        </button>
                    </div>
                )}

                {myStop.status === 'COMPLETED' && (
                    <div style={successBox}>✅ Enfant récupéré. Bonne journée !</div>
                )}
            </div>
            <button
                onClick={handleRunExportBatch}
                style={{backgroundColor: '#8e44ad', color: 'white', padding: '10px'}}
            >
                📊 EXPORTER LES PÉNALITÉS (BATCH)
            </button>
            {/* CARTE EN TEMPS RÉEL */}
            <div style={mapContainerStyle}>
                <MapContainer center={[33.5892, -7.6050]} zoom={15} style={{height: '100%', width: '100%'}}>
                    <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"/>

                    <MapFollower position={busPos}/>

                    {/* Le Bus */}
                    <Marker position={[busPos.latitude, busPos.longitude]} icon={busIcon}>
                        <Popup>Bus Scolaire</Popup>
                    </Marker>

                    {/* Votre Maison */}
                    <Marker position={[myStop.parent.latitude, myStop.parent.longitude]} icon={homeIcon}>
                        <Popup>Ma Maison</Popup>
                    </Marker>
                </MapContainer>
            </div>
        </div>
    );
}

// --- STYLES ---
const containerStyle = {display: 'flex', height: '100vh', width: '100vw', fontFamily: 'Arial'};
const sidebarStyle = {
    width: '350px',
    backgroundColor: 'white', padding: '25px', boxShadow: '2px 0 10px rgba(0,0,0,0.1)', zIndex: 1000 };
const mapContainerStyle = { flex: 1, backgroundColor: '#ddd' };
const infoBox = { padding: '15px', backgroundColor: '#3498db', color: 'white', borderRadius: '10px', textAlign: 'center' };
const arrivalBox = { padding: '20px', backgroundColor: '#f1c40f', borderRadius: '10px', textAlign: 'center' };
const timerStyle = { fontSize: '2.5em', fontWeight: 'bold', margin: '10px 0' };
const confirmBtnStyle = { backgroundColor: '#27ae60', color: 'white', border: 'none', padding: '15px', borderRadius: '8px', cursor: 'pointer', width: '100%', fontWeight: 'bold' };
const successBox = { padding: '20px', backgroundColor: '#2ecc71', color: 'white', borderRadius: '10px', textAlign: 'center' };
