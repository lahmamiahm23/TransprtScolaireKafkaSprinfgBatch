package com.example.transport.services;

import com.example.transport.Repositories.ParentRepository;
import com.example.transport.Repositories.PenaltyRepository;
import com.example.transport.Repositories.TripStopRepository;
import com.example.transport.entitie.Parent;
import com.example.transport.entitie.Penalty;
import com.example.transport.entitie.TripStop;
import com.example.transport.entitie.enumeration.TripStopStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate; // Pour le temps réel
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor // Remplace @Autowired pour un code plus propre
public class PenaltyService {

    private final PenaltyRepository penaltyRepository;
    private final ParentRepository parentRepository;
    private final TripStopRepository tripStopRepository;
    private final SimpMessagingTemplate messagingTemplate; // Injection pour WebSocket

    /**
     * 🔥 Méthode attendue par le Simulateur
     * Crée la pénalité et informe le Dashboard en temps réel
     */
    @Transactional
    public void createPenalty(Parent parent, String reason, Double amount) {
        Penalty penalty = new Penalty();
        penalty.setParent(parent);
        penalty.setReason(reason);
        penalty.setAmount(amount);
        penalty.setStatus("PENDING");
        penalty.setDateIssued(LocalDateTime.now());

        penaltyRepository.save(penalty);

        // Envoyer l'alerte au Dashboard via WebSocket
        Map<String, Object> alert = new HashMap<>();
        alert.put("type", "PENALTY_ALERT");
        alert.put("message", "Pénalité de " + amount + " DH pour " + parent.getFirstName());
        alert.put("parentId", parent.getId());

        messagingTemplate.convertAndSend("/topic/alerts", alert);
    }

    /**
     * Marquer une pénalité comme payée
     */
    public void markAsPaid(Long penaltyId) {
        Penalty penalty = penaltyRepository.findById(penaltyId)
                .orElseThrow(() -> new RuntimeException("Pénalité introuvable id: " + penaltyId));
        penalty.setStatus("PAID");
        penaltyRepository.save(penalty);
    }

    /**
     * Récupérer toutes les pénalités d'un parent
     */
    public List<Penalty> getPenaltiesByParent(Long parentId) {
        return penaltyRepository.findByParentId(parentId);
    }

    @Transactional
    public void clearPenaltyForOnTimePickup(Long parentId, Long tripStopId) {
        TripStop tripStop = tripStopRepository.findById(tripStopId)
                .orElseThrow(() -> new RuntimeException("Arrêt introuvable"));

        tripStop.setChildPickupTime(LocalDateTime.now());
        tripStop.setStatus(TripStopStatus.COMPLETED);
        tripStopRepository.save(tripStop);

        // Annuler les pénalités en attente si le parent arrive finalement
        List<Penalty> penalties = penaltyRepository.findByParentId(parentId);
        penalties.stream()
                .filter(p -> "PENDING".equals(p.getStatus()))
                .forEach(p -> {
                    p.setStatus("CANCELLED");
                    penaltyRepository.save(p);
                });
    }
}
