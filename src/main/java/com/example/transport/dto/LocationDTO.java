package com.example.transport.dto;

import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@NoArgsConstructor
public class LocationDTO {
    private Long vehicleId;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private String status; // AJOUTÉ : Pour le suivi temps réel (EN_ROUTE, ARRIVED)
    private LocalDateTime timestamp;



    public LocationDTO(Long vehicleId, Double latitude, Double longitude, Double speed, String status, LocalDateTime timestamp) {
        this.vehicleId = vehicleId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters et Setters existants...

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
