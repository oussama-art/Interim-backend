package com.TroisN.Service.repository;

import com.TroisN.Service.entity.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment,Long> {
    boolean existsByCandidate_IdAndEndDateAfter(Long candidateId, LocalDate date);
    @EntityGraph(attributePaths = {
            "candidate",
            "demande",
            "client"
    })
    Page<Assignment> findAll(Pageable pageable);

}
