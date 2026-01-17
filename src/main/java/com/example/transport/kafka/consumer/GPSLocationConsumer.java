package com.example.transport.kafka.consumer; // Vérifiez si c'est consumer ou producer dans votre dossier

import com.example.transport.dto.LocationDTO;
import com.example.transport.services.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GPSLocationConsumer {

    private final TrackingService trackingService;

    @KafkaListener(topics = "gps-location", groupId = "transport-group")
    public void consume(LocationDTO location) {
        log.info("📥 Réception position Kafka pour véhicule {}: [{}, {}]",
                location.getVehicleId(), location.getLatitude(), location.getLongitude());

        // 1️⃣ Sauvegarde de la position en base de données
        // + Envoi de la position aux WebSockets via TrackingService
        try {
            trackingService.processNewLocation(location);
        } catch (Exception e) {
            log.error("❌ Erreur lors du traitement de la position reçue de Kafka: {}", e.getMessage());
        }
    }
}
