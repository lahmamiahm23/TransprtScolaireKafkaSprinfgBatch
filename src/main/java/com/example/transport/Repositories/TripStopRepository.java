package com.example.transport.Repositories;

import com.example.transport.entitie.TripStop;
import com.example.transport.entitie.enumeration.TripStopStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TripStopRepository extends JpaRepository<TripStop, Long> {

    // --- LOGIQUE SIMULATEUR (Recherche du prochain arrêt) ---

    /**
     * Trouve le premier arrêt pour un véhicule qui correspond à l'un des statuts fournis.
     * Utile pour trouver l'arrêt où le bus doit aller OU l'arrêt où il est déjà stationné.
     */
    Optional<TripStop> findFirstByTrip_Vehicle_IdAndStatusInOrderByStopOrderAsc(
            Long vehicleId,
            List<TripStopStatus> statuses
    );

    /**
     * Version simplifiée pour un seul statut (ex: trouver le prochain 'SCHEDULED')
     */
    Optional<TripStop> findFirstByTrip_Vehicle_IdAndStatusOrderByStopOrderAsc(
            Long vehicleId,
            TripStopStatus status
    );


    // --- LOGIQUE PARENT (Pour l'interface React Parent) ---

    /**
     * Récupère la liste des arrêts pour un parent spécifique.
     * Crucial pour résoudre l'erreur GET /api/trip-stops/parent/{id}
     */
    List<TripStop> findByParentId(Long parentId);

    /**
     * Version avec tri pour afficher le trajet le plus récent en premier
     */
    List<TripStop> findByParentIdOrderByScheduledArrivalDesc(Long parentId);


    // --- LOGIQUE ADMIN & MONITEUR ---

    /**
     * Liste tous les arrêts d'un trajet précis pour l'affichage sur la carte
     */
    List<TripStop> findByTripIdOrderByStopOrderAsc(Long tripId);

    /**
     * Trouve les arrêts actuellement en statut 'ARRIVED' pour vérifier
     * si le délai de 5 minutes est dépassé (Pénalités).
     */
    List<TripStop> findByStatus(TripStopStatus status);

    @Query("SELECT ts FROM TripStop ts WHERE ts.status = 'ARRIVED' AND ts.actualArrival IS NOT NULL")
    List<TripStop> findStopsToCheckForPenalties();
}
