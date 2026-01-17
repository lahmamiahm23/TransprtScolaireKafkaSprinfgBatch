package com.example.transport.entitie;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="vehicle")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Vehicle {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="plate_number", nullable=false, unique=true)
    private String plateNumber;

    private String model;

    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="driver_id")
    @JsonIgnoreProperties("vehicle")
    private Driver driver;

    @OneToMany(mappedBy="vehicle", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    @JsonIgnoreProperties("vehicle")
    private List<Trip> trips = new ArrayList<>();

    @OneToMany(mappedBy="vehicle", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    @JsonIgnoreProperties("vehicle")
    private List<GPSLocation> locationHistory = new ArrayList<>();

    public Vehicle() {}

    public Vehicle(String plateNumber, String model, Driver driver) {
        this.plateNumber = plateNumber;
        this.model = model;
        this.driver = driver;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public List<Trip> getTrips() {
        return trips;
    }

    public void setTrips(List<Trip> trips) {
        this.trips = trips;
    }

    public List<GPSLocation> getLocationHistory() {
        return locationHistory;
    }

    public void setLocationHistory(List<GPSLocation> locationHistory) {
        this.locationHistory = locationHistory;
    }
}
