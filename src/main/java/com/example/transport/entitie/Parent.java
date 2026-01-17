package com.example.transport.entitie;

import com.example.transport.entitie.enumeration.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "parent")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Parent extends User {

    @Column(nullable = true)
    private Double latitude;

    @Column(nullable = true)
    private Double longitude;

    // Changez ici : @JsonIgnore au lieu de @JsonIgnoreProperties
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TripStop> tripStops = new ArrayList<>();
    // ================== Constructeurs ==================

    public Parent() {
        super();
    }

    /**
     * Constructeur complet pour créer un parent
     * @param firstName prénom
     * @param lastName nom
     * @param email email
     * @param username username (doit être non null)
     * @param phone téléphone
     * @param password mot de passe
     * @param latitude latitude
     * @param longitude longitude
     */
    public Parent(String firstName, String lastName, String email, String username,
                  String phone, String password, Double latitude, Double longitude) {
        super(firstName, lastName, email, username, phone, password, UserRole.PARENT);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // ================== Getters / Setters ==================

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public List<TripStop> getTripStops() {
        return tripStops;
    }

    public void setTripStops(List<TripStop> tripStops) {
        this.tripStops = tripStops;
    }
}
