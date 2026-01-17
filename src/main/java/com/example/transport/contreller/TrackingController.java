package com.example.transport.contreller;

import com.example.transport.Repositories.TripRepository;
import com.example.transport.entitie.Trip;
import com.example.transport.entitie.enumeration.TripStatus;
import com.example.transport.kafka.producer.GPSLocationProducer;
import com.example.transport.services.TripSimulatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class TrackingController {

    private final GPSLocationProducer gpsProducer;
    private final TripRepository tripRepository;
    private final TripSimulatorService tripSimulatorService; // Ajoute ton service de simulation

    @PostMapping("/start-simulation")
    public ResponseEntity<String> startSimulation() {
        // 1. Logique métier pour réinitialiser le trajet
        Trip trip = tripRepository.findById(1L).orElseThrow();
        trip.setStatus(TripStatus.IN_PROGRESS);
        trip.setStartTime(LocalDateTime.now());
        tripRepository.save(trip);

        // 2. LANCER LE THREAD DE SIMULATION KAFKA
        // C'est ici que les points GPS vont commencer à être envoyés
        tripSimulatorService.startTrackingSimulation(1L);

        return ResponseEntity.ok("Simulation démarrée et flux Kafka lancé");
    }
}
