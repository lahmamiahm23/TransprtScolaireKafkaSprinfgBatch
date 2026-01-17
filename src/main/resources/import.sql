-- Nettoyage pour les tests (Optionnel)
TRUNCATE trip_stop, trip, vehicle, driver, parent, users CASCADE;

-- 1. Utilisateurs (Ajout de 'updated_at' pour la cohérence Hibernate)
INSERT INTO users (id, first_name, last_name, email, username, phone, password, role, created_at, updated_at)
VALUES
    (1, 'Yassine', 'El Amrani', 'yassine@email.com', 'parent1', '0601020304', 'pass123', 'PARENT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Fatima', 'Zahra', 'fatima@email.com', 'parent2', '0605060708', 'pass456', 'PARENT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'Driss', 'Berrada', 'driss@bus.com', 'driver1', '0611223344', 'driverpass', 'DRIVER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2. Parents (Points de passage sur la carte)
-- Coordonnées réelles à Casablanca pour un mouvement visible
INSERT INTO parent (id, latitude, longitude) VALUES
                                                 (1, 33.5892, -7.6050), -- Arrêt 1: Près du Twin Center
                                                 (2, 33.5750, -7.6350); -- Arrêt 2: Quartier Maârif

-- 3. Chauffeur
INSERT INTO driver (id, license_number) VALUES (3, 'ABC12345');

-- 4. Véhicule
INSERT INTO vehicle (id, plate_number, model, driver_id) VALUES (1, '12345-A-01', 'Mercedes Sprinter', 3);

-- 5. Trajet (Initialisé en SCHEDULED pour laisser le bouton React le démarrer)
INSERT INTO trip (id, vehicle_id, status, start_time)
VALUES (1, 1, 'SCHEDULED', NULL);

-- 6. Arrêts (TripStops)
-- On initialise actual_arrival à NULL car c'est le simulateur qui le remplira
INSERT INTO trip_stop (id, trip_id, parent_id, stop_order, status, scheduled_arrival, actual_arrival)
VALUES
    (1, 1, 1, 1, 'SCHEDULED', CURRENT_TIMESTAMP + interval '10 minutes', NULL),
    (2, 1, 2, 2, 'SCHEDULED', CURRENT_TIMESTAMP + interval '20 minutes', NULL);

-- 7. IMPORTANT : Mise à jour des séquences d'ID (pour éviter les erreurs Primary Key sur les futurs inserts)
SELECT setval('users_id_seq', (SELECT max(id) FROM users));
SELECT setval('trip_id_seq', (SELECT max(id) FROM trip));
SELECT setval('trip_stop_id_seq', (SELECT max(id) FROM trip_stop));
