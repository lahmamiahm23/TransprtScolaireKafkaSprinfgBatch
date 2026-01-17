package com.example.transport.Repositories;

import com.example.transport.entitie.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Trouver les notifications d'un utilisateur triées par date décroissante
     */
    List<Notification> findByUserIdOrderBySentAtDesc(Long userId);

    /**
     * Trouver les notifications non lues d'un utilisateur
     * CORRECTION: Utiliser "read" (boolean) au lieu de "Read" (avec majuscule)
     */
    List<Notification> findByUserIdAndReadFalseOrderBySentAtDesc(Long userId);

    /**
     * Compter les notifications non lues d'un utilisateur
     */
    long countByUserIdAndReadFalse(Long userId);

    /**
     * Trouver les notifications par type
     */
    List<Notification> findByUserIdAndTypeOrderBySentAtDesc(Long userId, com.example.transport.entitie.enumeration.NotificationType type);

    /**
     * Trouver les notifications non lues par type
     */
    List<Notification> findByUserIdAndTypeAndReadFalseOrderBySentAtDesc(Long userId, com.example.transport.entitie.enumeration.NotificationType type);
}
