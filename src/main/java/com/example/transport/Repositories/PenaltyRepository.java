package com.example.transport.Repositories;

import com.example.transport.entitie.Penalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PenaltyRepository extends JpaRepository<Penalty, Long> {

    /**
     * Récupère toutes les pénalités associées à un parent
     */
    List<Penalty> findByParentId(Long parentId);

    /**
     * Récupère toutes les pénalités par statut (ex: "PENDING", "PAID", "CANCELLED")
     * Ici le statut est un String dans Penalty après suppression de l'enum PenaltyStatus
     */
    List<Penalty> findByStatus(String status);

    /**
     * Récupère toutes les pénalités pour un parent donné avec un statut spécifique
     */
    List<Penalty> findByParentIdAndStatus(Long parentId, String status);
}
