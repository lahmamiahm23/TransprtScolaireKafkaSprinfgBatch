package com.example.transport.dto;

import com.example.transport.entitie.TripStop;
import lombok.Data;
import java.time.LocalDateTime;

public class TripStopDTO {

    private Long id;
    private Long tripId;

    private Long parentId;
    private String parentName;

    private Double parentLatitude;
    private Double parentLongitude;

    private LocalDateTime actualArrival;
    private LocalDateTime pickupTime;

    private String status;
    private Long remainingSeconds; // pour le timer 5 min

    public TripStopDTO(TripStop stop) {
        this.id = stop.getId();
        this.tripId = stop.getTrip().getId();
        this.parentId = stop.getParent().getId();
        this.parentName = stop.getParent().getFirstName();

        this.parentLatitude = stop.getParent().getLatitude();
        this.parentLongitude = stop.getParent().getLongitude();

        this.actualArrival = stop.getActualArrival();
        this.pickupTime = stop.getChildPickupTime();
        this.status = stop.getStatus().name();

        if (stop.getActualArrival() != null) {
            long seconds =
                    300 - java.time.Duration.between(
                            stop.getActualArrival(),
                            LocalDateTime.now()
                    ).getSeconds();
            this.remainingSeconds = Math.max(seconds, 0);
        }
    }

    public TripStopDTO() {
    }

    public TripStopDTO(Long id, Long parentId, Long tripId, String parentName, Double parentLatitude, Double parentLongitude, LocalDateTime actualArrival, LocalDateTime pickupTime, String status, Long remainingSeconds) {
        this.id = id;
        this.parentId = parentId;
        this.tripId = tripId;
        this.parentName = parentName;
        this.parentLatitude = parentLatitude;
        this.parentLongitude = parentLongitude;
        this.actualArrival = actualArrival;
        this.pickupTime = pickupTime;
        this.status = status;
        this.remainingSeconds = remainingSeconds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(Long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(LocalDateTime pickupTime) {
        this.pickupTime = pickupTime;
    }

    public LocalDateTime getActualArrival() {
        return actualArrival;
    }

    public void setActualArrival(LocalDateTime actualArrival) {
        this.actualArrival = actualArrival;
    }

    public Double getParentLongitude() {
        return parentLongitude;
    }

    public void setParentLongitude(Double parentLongitude) {
        this.parentLongitude = parentLongitude;
    }

    public Double getParentLatitude() {
        return parentLatitude;
    }

    public void setParentLatitude(Double parentLatitude) {
        this.parentLatitude = parentLatitude;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }
}
