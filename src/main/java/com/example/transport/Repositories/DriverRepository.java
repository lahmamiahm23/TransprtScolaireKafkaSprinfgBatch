package com.example.transport.Repositories;

import com.example.transport.entitie.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    boolean existsByEmail(String email);

    Optional<Driver> findByEmail(String email);
}
