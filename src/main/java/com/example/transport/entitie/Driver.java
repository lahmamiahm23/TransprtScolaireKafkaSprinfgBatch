package com.example.transport.entitie;

import com.example.transport.entitie.enumeration.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

/**
 * Entité Driver héritant de User
 */
@Entity
@Table(name = "driver")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Driver extends User {

    @Column(name = "license_number", nullable = false, unique = true)
    private String licenseNumber;

    @OneToOne(mappedBy = "driver", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"driver", "trips", "locationHistory"})
    private Vehicle vehicle;

    // =================== Constructeurs ===================

    public Driver() {
        super();
    }

    /**
     * Constructeur minimal pour licenseNumber
     */
    public Driver(String licenseNumber) {
        super();
        this.licenseNumber = licenseNumber;
    }

    /**
     * Constructeur complet incluant username
     */
    public Driver(String firstName, String lastName, String email, String username,
                  String phone, String password, String licenseNumber) {
        super(firstName, lastName, email, username, phone, password, UserRole.DRIVER);
        this.licenseNumber = licenseNumber;
    }

    // =================== Getters / Setters ===================

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
}
