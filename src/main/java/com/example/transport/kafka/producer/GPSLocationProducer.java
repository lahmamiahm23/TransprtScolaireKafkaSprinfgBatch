package com.example.transport.kafka.producer;

import com.example.transport.dto.LocationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GPSLocationProducer {

    private final KafkaTemplate<String, LocationDTO> kafkaTemplate;

    public void sendLocation(LocationDTO location) {
        kafkaTemplate.send("gps-location", location.getVehicleId().toString(), location);
    }
}

