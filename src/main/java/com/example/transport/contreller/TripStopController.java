package com.example.transport.contreller;

import com.example.transport.Repositories.TripStopRepository;
import com.example.transport.entitie.TripStop;
import com.example.transport.entitie.enumeration.TripStopStatus;
import com.example.transport.services.TripStopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trip-stops")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS})
public class TripStopController {

    private final TripStopRepository tripStopRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final TripStopService tripStopService;

    // --- EXISTANT : Pour le Dashboard Admin ---
    @GetMapping("/trip/{tripId}")
    public List<TripStop> getStopsByTrip(@PathVariable Long tripId) {
        return tripStopRepository.findByTripIdOrderByStopOrderAsc(tripId);
    }

    // --- NOUVEAU : Pour la Vue Parent (Résout l'erreur 404/CORS) ---
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<TripStop> getStopByParent(@PathVariable Long parentId) {
        // On cherche l'arrêt pour ce parent qui n'est pas encore terminé
        return tripStopRepository.findByParentId(parentId)
                .stream()
                .filter(stop -> stop.getStatus() != TripStopStatus.COMPLETED)
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- EXISTANT : Pour confirmer la montée ---
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeStop(@PathVariable Long id) {
        TripStop stop = tripStopRepository.findById(id).orElseThrow();
        stop.setStatus(TripStopStatus.COMPLETED);
        tripStopRepository.save(stop);

        // Envoyer un signal aux deux : Admin et Parent
        messagingTemplate.convertAndSend("/topic/bus-location", "REFRESH");
        return ResponseEntity.ok().build();
    }
    @PostMapping("/reset-demo")
    public ResponseEntity<String> resetDemoData() {
        // 1. Remettre les arrêts en SCHEDULED
        List<TripStop> stops = tripStopRepository.findAll();
        stops.forEach(stop -> {
            stop.setStatus(TripStopStatus.SCHEDULED);
            stop.setActualArrival(null);
        });
        tripStopRepository.saveAll(stops);

        // 2. Remettre le trajet en SCHEDULED (ou IN_PROGRESS)
        // tripRepository.updateStatus(1L, TripStatus.SCHEDULED);

        return ResponseEntity.ok("Données réinitialisées avec succès");
    }
}
