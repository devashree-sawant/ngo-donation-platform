package com.ngo.ngoplatform.repository;

import com.ngo.ngoplatform.entity.Contribution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContributionRepository extends JpaRepository<Contribution, Long> {
    List<Contribution> findByDonorIdOrderByDonationDateDescIdDesc(Long donorId);
    List<Contribution> findByNeedId(Long needId);
    boolean existsByNeedId(Long needId);
    boolean existsByDonorId(Long donorId);
}
