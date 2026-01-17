package com.example.transport.Repositories;

import com.example.transport.entitie.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParentRepository extends JpaRepository<Parent, Long> {

    boolean existsByEmail(String email);

    Optional<Parent> findByEmail(String email);
}
