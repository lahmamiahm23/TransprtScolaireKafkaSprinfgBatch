package com.example.transport.Repositories;

import com.example.transport.entitie.GPSLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GPSLocationRepository extends JpaRepository<GPSLocation, Long> {

    // CORRECTION : Utiliser List au lieu de GPSLocation unique
    @Query("SELECT g FROM GPSLocation g WHERE g.vehicle.id = :vehicleId ORDER BY g.timestamp DESC")
    List<GPSLocation> findByVehicleIdOrderByTimestampDesc(@Param("vehicleId") Long vehicleId);

    // NOUVELLE méthode : Récupérer seulement la première position
    @Query(value = "SELECT * FROM gpslocation WHERE vehicle_id = :vehicleId ORDER BY timestamp DESC LIMIT 1",
            nativeQuery = true)
    GPSLocation findLatestByVehicleId(@Param("vehicleId") Long vehicleId);

    // Méthode alternative avec Optional
    @Query("SELECT g FROM GPSLocation g WHERE g.vehicle.id = :vehicleId ORDER BY g.timestamp DESC")
    List<GPSLocation> findAllByVehicleIdOrderByTimestamp(@Param("vehicleId") Long vehicleId);

    default Optional<GPSLocation> findFirstByVehicleIdOrderByTimestampDesc(Long vehicleId) {
        List<GPSLocation> locations = findAllByVehicleIdOrderByTimestamp(vehicleId);
        return locations.isEmpty() ? Optional.empty() : Optional.of(locations.get(0));
    }

    // Historique pour un trajet entre deux dates
    @Query("SELECT g FROM GPSLocation g WHERE g.vehicle.id = :vehicleId " +
            "AND g.timestamp BETWEEN :start AND :end " +
            "ORDER BY g.timestamp DESC")
    List<GPSLocation> findByVehicleIdAndTimestampBetween(
            @Param("vehicleId") Long vehicleId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Positions récentes (dernières 24h)
    @Query("SELECT g FROM GPSLocation g WHERE g.vehicle.id = :vehicleId " +
            "AND g.timestamp >= :since " +
            "ORDER BY g.timestamp DESC")
    List<GPSLocation> findRecentByVehicleId(
            @Param("vehicleId") Long vehicleId,
            @Param("since") LocalDateTime since);
}
