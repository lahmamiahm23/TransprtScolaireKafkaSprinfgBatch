package com.example.transport.entitie;

import com.example.transport.entitie.enumeration.TripStopStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "trip_stop",
        indexes = {
                @Index(name = "idx_trip_stop_trip", columnList = "trip_id"),
                @Index(name = "idx_trip_stop_parent", columnList = "parent_id"),
                @Index(name = "idx_trip_stop_status", columnList = "status")
        }
)
public class TripStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ===================== RELATIONS ===================== */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    @JsonIgnore // Empêche la boucle infinie avec Trip
    private Trip trip;

    // CHANGEMENT ICI : FetchType.EAGER pour éviter l'erreur "no Session"
    // On ignore aussi le champ "tripStops" à l'intérieur de Parent s'il existe
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "parent_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "tripStops"})
    private Parent parent;

    /* ===================== ORDRE DE PASSAGE ===================== */

    @Column(name = "stop_order", nullable = false)
    private Integer stopOrder;

    /* ===================== TEMPS ===================== */

    @Column(name = "scheduled_arrival")
    private LocalDateTime scheduledArrival;

    @Column(name = "actual_arrival")
    private LocalDateTime actualArrival;

    @Column(name = "child_pickup_time")
    private LocalDateTime childPickupTime;

    /* ===================== STATUT ===================== */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStopStatus status = TripStopStatus.SCHEDULED;

    @Column(length = 500)
    private String notes;

    /* ===================== CONSTRUCTEURS ===================== */

    public TripStop() {}

    /* ===================== GETTERS / SETTERS ===================== */

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) { this.trip = trip; }

    public Parent getParent() { return parent; }
    public void setParent(Parent parent) { this.parent = parent; }

    public Integer getStopOrder() { return stopOrder; }
    public void setStopOrder(Integer stopOrder) { this.stopOrder = stopOrder; }

    public LocalDateTime getScheduledArrival() { return scheduledArrival; }
    public void setScheduledArrival(LocalDateTime scheduledArrival) { this.scheduledArrival = scheduledArrival; }

    public LocalDateTime getActualArrival() { return actualArrival; }
    public void setActualArrival(LocalDateTime actualArrival) { this.actualArrival = actualArrival; }

    public LocalDateTime getChildPickupTime() { return childPickupTime; }
    public void setChildPickupTime(LocalDateTime childPickupTime) { this.childPickupTime = childPickupTime; }

    public TripStopStatus getStatus() { return status; }
    public void setStatus(TripStopStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
