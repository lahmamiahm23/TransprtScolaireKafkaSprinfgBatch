package com.example.transport.services;

import com.example.transport.Repositories.TripStopRepository;
import com.example.transport.entitie.Parent;
import com.example.transport.entitie.TripStop;
import com.example.transport.entitie.enumeration.TripStopStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor // Remplace @Autowired pour un code plus propre
public class TripStopService {

    private final TripStopRepository tripStopRepository;
    private final PenaltyService penaltyService;

    /**
     * Appelé par le simulateur quand le bus arrive aux coordonnées GPS du parent.
     */
    @Transactional
    public TripStop arriveAtStop(TripStop stop) {
        stop.setActualArrival(LocalDateTime.now());
        stop.setStatus(TripStopStatus.ARRIVED); // Changé de WAITING à ARRIVED pour cohérence Front-end
        return tripStopRepository.save(stop);
    }

    /**
     * Appelé quand le parent clique sur "J'ai récupéré mon enfant" dans React.
     */
    @Transactional
    public TripStop markChildBoarded(Long stopId) {
        TripStop tripStop = tripStopRepository.findById(stopId)
                .orElseThrow(() -> new RuntimeException("Arrêt non trouvé avec l'ID : " + stopId));

        LocalDateTime now = LocalDateTime.now();

        // Sécurité : vérifier si actualArrival est nul
        if (tripStop.getActualArrival() == null) {
            tripStop.setActualArrival(now);
        }

        long minutesWaited = ChronoUnit.MINUTES.between(tripStop.getActualArrival(), now);

        if (minutesWaited >= 5) {
            // Le parent a cliqué, mais après le délai de 5 minutes
            Parent parent = tripStop.getParent();
            penaltyService.createPenalty(parent, "Retard à l'embarquement (> 5 min)", 50.0);

            tripStop.setStatus(TripStopStatus.COMPLETED); // On met COMPLETED pour libérer le bus
            tripStop.setNotes("Embarquement tardif (" + minutesWaited + " min) - Pénalité enregistrée");
        } else {
            // Embarquement normal
            tripStop.setStatus(TripStopStatus.COMPLETED);
            tripStop.setNotes("Embarquement réussi en " + minutesWaited + " minutes");
        }

        tripStop.setChildPickupTime(now);
        return tripStopRepository.save(tripStop);
    }
}
