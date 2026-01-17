package com.example.transport.contreller;

import com.example.transport.Repositories.ParentRepository;
import com.example.transport.entitie.Parent;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ParentController {

    private final ParentRepository parentRepository;

    // --- AJOUTEZ CETTE MÉTHODE ---
    @GetMapping
    public java.util.List<Parent> getAllParents() {
        return parentRepository.findAll();
    }
    // ----------------------------

    @PostMapping
    public Parent createParent(@RequestBody Parent parent) {
        return parentRepository.save(parent);
    }

    @PutMapping("/{parentId}/location")
    public Parent updateLocation(
            @PathVariable Long parentId,
            @RequestParam double latitude,
            @RequestParam double longitude) {

        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent introuvable"));

        parent.setLatitude(latitude);
        parent.setLongitude(longitude);

        return parentRepository.save(parent);
    }
}
