package com.TroisN.Service.repository;

import com.TroisN.Service.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    boolean existsByEmailAddress(String emailAddress);

    @Query("""
        SELECT COUNT(oc) > 0
        FROM OfferCandidate oc
        JOIN oc.offer o
        WHERE oc.candidate.id = :candidateId
          AND o.client.id = :clientId
    """)
    boolean existsCandidateAppliedToClientOffers(
            @Param("candidateId") Long candidateId,
            @Param("clientId") Long clientId
    );

    @Query("""
        SELECT c
        FROM Candidate c
        WHERE NOT EXISTS (
            SELECT a
            FROM Assignment a
            WHERE a.candidate = c
              AND a.startDate <= :endDate
              AND a.endDate >= :startDate
        )
    """)
    List<Candidate> findAvailableCandidatesInPeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /*
    ===== À remettre plus tard avec Keycloak =====

    Optional<Candidate> findByKeycloakUserId(String keycloakUserId);

    void deleteByKeycloakUserId(String keycloakUserId);
    */

    boolean existsByFirstNameAndLastName(String firstName, String lastName);
}