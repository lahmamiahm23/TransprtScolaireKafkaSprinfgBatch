package com.example.transport.contreller;

import com.example.transport.entitie.Penalty;
import com.example.transport.services.PenaltyService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/penalties")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class PenaltyController {

    private final PenaltyService penaltyService;

    @GetMapping("/parent/{parentId}")
    public List<Penalty> getPenalties(@PathVariable Long parentId) {
        return penaltyService.getPenaltiesByParent(parentId);
    }

    @PostMapping("/{penaltyId}/pay")
    public ResponseEntity<Void> payPenalty(@PathVariable Long penaltyId) {
        penaltyService.markAsPaid(penaltyId);
        return ResponseEntity.ok().build();
    }
}
