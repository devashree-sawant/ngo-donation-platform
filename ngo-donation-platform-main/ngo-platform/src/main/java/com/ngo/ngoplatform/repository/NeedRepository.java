package com.ngo.ngoplatform.repository;

import com.ngo.ngoplatform.entity.Need;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NeedRepository extends JpaRepository<Need, Long> {
    List<Need> findByNgoId(Long ngoId);
    boolean existsByNgoId(Long ngoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from Need n where n.id = :id")
    Optional<Need> findByIdForUpdate(@Param("id") Long id);
}
