package com.ngo.ngoplatform.repository;

import com.ngo.ngoplatform.entity.Donor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DonorRepository extends JpaRepository<Donor, Long> {
    Optional<Donor> findByEmail(String email);
    Optional<Donor> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
