package com.example.transport.services;

import com.example.transport.Repositories.GPSLocationRepository;
import com.example.transport.Repositories.TripStopRepository;
import com.example.transport.Repositories.VehicleRepository;
import com.example.transport.dto.LocationDTO;
import com.example.transport.entitie.GPSLocation;
import com.example.transport.entitie.Parent;
import com.example.transport.entitie.TripStop;
import com.example.transport.entitie.Vehicle;
import com.example.transport.entitie.enumeration.TripStopStatus;
import com.example.transport.utils.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingService {

    private static final double ARRIVAL_THRESHOLD_KM = 0.05; // 50 mètres
    private static final double PROXIMITY_THRESHOLD_KM = 0.5; // 500 mètres pour alerte approche

    private final GPSLocationRepository gpsRepository;
    private final VehicleRepository vehicleRepository;
    private final TripStopRepository tripStopRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Traite la nouvelle position GPS reçue du bus
     */
    @Transactional
    public void processNewLocation(LocationDTO location) {
        // 1. Charger le véhicule et sauvegarder la position
        Vehicle vehicle = vehicleRepository.findById(location.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Véhicule " + location.getVehicleId() + " introuvable"));

        saveGpsHistory(vehicle, location);

        // 2. Diffuser la position sur le Dashboard Global (Admin/Ecole)
        messagingTemplate.convertAndSend("/topic/bus-location", location);

        // 3. Identifier l'arrêt actuel (celui qui est SCHEDULED ou WAITING)
        tripStopRepository.findFirstByTrip_Vehicle_IdAndStatusInOrderByStopOrderAsc(
                vehicle.getId(),
                List.of(TripStopStatus.SCHEDULED, TripStopStatus.WAITING, TripStopStatus.ARRIVED)
        ).ifPresent(stop -> {
            processStopProximity(location, stop);
        });
    }

    private void processStopProximity(LocationDTO location, TripStop stop) {
        Parent parent = stop.getParent();
        if (parent.getLatitude() == null || parent.getLongitude() == null) return;

        double distance = GeoUtils.distanceKm(
                location.getLatitude(), location.getLongitude(),
                parent.getLatitude(), parent.getLongitude()
        );

        // --- CAS 1 : LE BUS ARRIVE (Moins de 50m) ---
        if (distance <= ARRIVAL_THRESHOLD_KM && stop.getStatus() != TripStopStatus.ARRIVED) {
            handleArrival(location, stop, parent);
        }
        // --- CAS 2 : LE BUS APPROCHE (Moins de 500m) ---
        else if (distance <= PROXIMITY_THRESHOLD_KM) {
            location.setStatus("APPROACHING");
            sendNotificationToParent(parent.getId(), "Le bus est à moins de 500m ! Préparez-vous.");
        }
        // --- CAS 3 : EN ROUTE ---
        else {
            location.setStatus("EN_ROUTE");
        }

        // Push de la position spécifique au parent (pour sa Map personnelle)
        messagingTemplate.convertAndSend("/topic/parent/" + parent.getId() + "/vehicle", location);
    }

    private void handleArrival(LocationDTO location, TripStop stop, Parent parent) {
        log.info("Bus {} arrivé chez le parent {}", location.getVehicleId(), parent.getLastName());

        location.setStatus("ARRIVED");
        location.setSpeed(0.0);

        // Mise à jour en base de l'arrêt
        stop.setStatus(TripStopStatus.ARRIVED);
        stop.setActualArrival(LocalDateTime.now());
        tripStopRepository.save(stop);

        // Alerte temps réel au parent
        sendNotificationToParent(parent.getId(), "Le bus est devant votre porte !");
    }

    private void saveGpsHistory(Vehicle vehicle, LocationDTO location) {
        GPSLocation gps = new GPSLocation();
        gps.setVehicle(vehicle);
        gps.setLatitude(location.getLatitude());
        gps.setLongitude(location.getLongitude());
        gps.setTimestamp(LocalDateTime.now());
        gpsRepository.save(gps);
    }

    private void sendNotificationToParent(Long parentId, String message) {
        messagingTemplate.convertAndSend("/topic/parent/" + parentId + "/notifications", message);
    }
}
