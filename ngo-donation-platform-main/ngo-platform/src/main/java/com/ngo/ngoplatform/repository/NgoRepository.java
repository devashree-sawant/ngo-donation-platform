package com.ngo.ngoplatform.repository;

import com.ngo.ngoplatform.entity.Ngo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NgoRepository extends JpaRepository<Ngo, Long> {
    Optional<Ngo> findByEmail(String email);
    Optional<Ngo> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
