package com.example.transport.entitie;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "penalty")
public class Penalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reason;

    private Double amount;

    private LocalDateTime dateIssued;

    // Champ string pour le statut : "PENDING", "PAID", "CANCELLED"
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Parent parent;

    // =================== Constructeurs ===================
    public Penalty() {}

    public Penalty(String reason, Double amount, LocalDateTime dateIssued, String status, Parent parent) {
        this.reason = reason;
        this.amount = amount;
        this.dateIssued = dateIssued;
        this.status = status;
        this.parent = parent;
    }

    // =================== Getters / Setters ===================
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDateTime getDateIssued() {
        return dateIssued;
    }

    public void setDateIssued(LocalDateTime dateIssued) {
        this.dateIssued = dateIssued;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Parent getParent() {
        return parent;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }
}
