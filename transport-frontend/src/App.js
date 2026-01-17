import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link, useParams } from 'react-router-dom'; // useParams déplacé ici
import TripDashboard from './components/TripDashboard';
import ParentPickupView from './components/ParentPickupView';

// --- COMPOSANT WRAPPER ---
// On le définit ici, en dehors de l'import, pour extraire l'ID de l'URL
function ParentPickupViewWrapper() {
    const { parentId } = useParams();
    return <ParentPickupView parentId={parentId} />;
}

function App() {
    return (
        <Router>
            <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', fontFamily: 'Arial' }}>

                {/* BARRE DE NAVIGATION (Utile pour tester les deux rôles) */}
                <nav style={navStyle}>
                    <div style={{ fontWeight: 'bold', fontSize: '1.2em' }}>🚌 SchoolBus App</div>
                    <div>
                        <Link to="/admin" style={linkStyle}>📊 Dashboard Admin</Link>
                        <Link to="/parent/1" style={linkStyle}>🏠 Parent Yassine (ID: 1)</Link>
                        <Link to="/parent/2" style={linkStyle}>🏠 Parent Fatima (ID: 2)</Link>
                    </div>
                </nav>

                {/* ZONE DE CONTENU */}
                <div style={{ flex: 1 }}>
                    <Routes>
                        <Route path="/admin" element={<TripDashboard />} />
                        <Route path="/parent/:parentId" element={<ParentPickupViewWrapper />} />
                        {/* Redirection par défaut vers l'admin */}
                        <Route path="/" element={<TripDashboard />} />
                    </Routes>
                </div>
            </div>
        </Router>
    );
}

// --- STYLES ---
const navStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '15px 30px',
    backgroundColor: '#2c3e50',
    color: 'white',
    boxShadow: '0 2px 5px rgba(0,0,0,0.2)'
};

const linkStyle = {
    color: '#ecf0f1',
    marginLeft: '20px',
    textDecoration: 'none',
    fontSize: '0.9em',
    padding: '5px 10px',
    borderRadius: '4px',
    border: '1px solid transparent',
    transition: '0.3s'
};

export default App;
