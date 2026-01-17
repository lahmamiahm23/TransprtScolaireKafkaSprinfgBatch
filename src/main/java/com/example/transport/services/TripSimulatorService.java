package com.example.transport.services;

import com.example.transport.Repositories.TripStopRepository;
import com.example.transport.dto.LocationDTO;
import com.example.transport.entitie.TripStop;
import com.example.transport.entitie.enumeration.TripStopStatus;
import com.example.transport.kafka.producer.GPSLocationProducer;
import com.example.transport.utils.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class TripSimulatorService {

    private final GPSLocationProducer gpsProducer;
    private final TripStopRepository tripStopRepository;
    private final PenaltyService penaltyService;
    private final SimpMessagingTemplate messagingTemplate;

    // État de la simulation (volatile pour la visibilité entre threads)
    private volatile boolean isSimulationRunning = false;
    private double currentLat = 33.595;
    private double currentLng = -7.600;

    public void startTrackingSimulation(Long tripId) {
        // CORRECTION 1 : Empêcher le reset si déjà en cours
        if (this.isSimulationRunning) {
            log.warn("La simulation tourne déjà pour le trajet {}. Requête ignorée.", tripId);
            return;
        }

        this.isSimulationRunning = true;
        // On initialise la position de départ une seule fois
        this.currentLat = 33.595;
        this.currentLng = -7.600;
        log.info("🚀 Simulation initialisée et démarrée pour le trajet ID: {}", tripId);
    }

    @Scheduled(fixedRate = 1000)
    @Transactional
    public void simulateBusMovement() {
        if (!isSimulationRunning) return;

        try {
            List<TripStop> stops = tripStopRepository.findByTripIdOrderByStopOrderAsc(1L);

            TripStop nextStop = stops.stream()
                    .filter(s -> s.getStatus() != TripStopStatus.COMPLETED)
                    .findFirst()
                    .orElse(null);

            if (nextStop == null) {
                log.info("🏁 Fin du trajet détectée.");
                isSimulationRunning = false;
                return;
            }

            double destLat = nextStop.getParent().getLatitude();
            double destLng = nextStop.getParent().getLongitude();
            double distance = GeoUtils.distanceKm(currentLat, currentLng, destLat, destLng);
            String currentStatus = "EN_ROUTE";

            // --- ZONE DE LOGIQUE ARRÊT ---
            if (distance < 0.03) { // 30 mètres
                currentLat = destLat;
                currentLng = destLng;
                currentStatus = "ARRIVED";

                // Si c'est la première fois qu'on arrive, on change le statut en ARRIVED
                if (nextStop.getStatus() == TripStopStatus.SCHEDULED) {
                    log.info("📍 Bus arrivé chez {}", nextStop.getParent().getLastName());
                    nextStop.setStatus(TripStopStatus.ARRIVED);
                    nextStop.setActualArrival(LocalDateTime.now());
                    tripStopRepository.saveAndFlush(nextStop);
                    messagingTemplate.convertAndSend("/topic/alerts", "Le bus est devant chez " + nextStop.getParent().getLastName());
                }



                broadcastLocation(currentStatus);
                checkForLateParents(); // Gère le passage à LATE_BOARDING après 5 min

                return; // ON BLOQUE ICI. Le bus ne bougera pas au bloc suivant (calcul mouvement).

            } else {
                // 3. Calcul du mouvement uniquement si on n'est pas à l'arrêt
                currentLat += (destLat - currentLat) * 0.1;
                currentLng += (destLng - currentLng) * 0.1;
            }

            broadcastLocation(currentStatus);

        } catch (Exception e) {
            log.error("❌ Erreur critique simulation: {}", e.getMessage());
        }
    }

    private void broadcastLocation(String status) {
        LocationDTO location = new LocationDTO(
                1L, currentLat, currentLng,
                status.equals("ARRIVED") ? 0.0 : 35.0,
                status, LocalDateTime.now()
        );

        // Envoi Kafka (Try-catch pour ne pas bloquer si Kafka est down)
        try {
            gpsProducer.sendLocation(location);
        } catch (Exception e) {
            log.warn("Kafka non disponible, envoi WebSocket uniquement.");
        }

        // Envoi WebSocket
        messagingTemplate.convertAndSend("/topic/bus-location", location);
    }

    private void checkForLateParents() {
        // On cherche les arrêts en cours d'attente
        List<TripStop> activeStops = tripStopRepository.findByStatus(TripStopStatus.ARRIVED);
        for (TripStop stop : activeStops) {
            if (stop.getActualArrival() != null) {
                long waitTime = Duration.between(stop.getActualArrival(), LocalDateTime.now()).toMinutes();
                if (waitTime >= 5 && stop.getStatus() != TripStopStatus.LATE_BOARDING) {
                    log.warn("⚠️ Pénalité pour le parent {}", stop.getParent().getLastName());
                    penaltyService.createPenalty(stop.getParent(), "Retard : " + waitTime + " min", 50.0);
                    stop.setStatus(TripStopStatus.LATE_BOARDING);
                    tripStopRepository.save(stop);
                    messagingTemplate.convertAndSend("/topic/alerts", "Pénalité pour " + stop.getParent().getFirstName());
                }
            }
        }
    }
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void resetAllData() {
        // Exécute ton script de nettoyage
        jdbcTemplate.execute("UPDATE trip_stop SET status = 'SCHEDULED', actual_arrival = NULL WHERE trip_id = 1");
        jdbcTemplate.execute("UPDATE trip SET status = 'SCHEDULED', start_time = NULL WHERE id = 1");

        // Tu peux aussi remettre le bus à sa position initiale si nécessaire
        log.info("♻️ Base de données réinitialisée pour les tests.");
    }
}
