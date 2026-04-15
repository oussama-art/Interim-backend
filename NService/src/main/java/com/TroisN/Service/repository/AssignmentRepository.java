package com.TroisN.Service.repository;

import com.TroisN.Service.entity.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment,Long> {
    boolean existsByCandidate_IdAndEndDateAfter(Long candidateId, LocalDate date);
    @EntityGraph(attributePaths = {
            "candidate",
            "demande",
            "client"
    })
    Page<Assignment> findAll(Pageable pageable);

    @Query("""
    SELECT a FROM Assignment a
    WHERE a.candidate.id = :candidateId
      AND a.endDate >= :startDate
      AND a.startDate <= :endDate
""")
    List<Assignment> findOverlappingAssignments(
            @Param("candidateId") Long candidateId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
    SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
    FROM Assignment a
    WHERE a.candidate.id = :candidateId
      AND a.endDate >= :startDate
      AND a.startDate <= :endDate
""")
    boolean existsOverlappingAssignment(
            @Param("candidateId") Long candidateId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    void deleteByDemande_Id(Long demandeId);

    Optional<Assignment> findByCandidate_IdAndDemande_Id(Long candidateId, Long demandeId);

    List<Assignment> findByDemande_Id(Long demandeId);

    boolean existsByCandidate_Id(Long candidateId);

    boolean existsByCandidate_IdAndDemande_IdNot(Long candidateId, Long demandeId);




}
