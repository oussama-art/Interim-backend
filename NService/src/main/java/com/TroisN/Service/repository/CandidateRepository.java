package com.TroisN.Service.repository;

import com.TroisN.Service.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    boolean existsByEmailAddress(String emailAddress);

    Optional<Candidate> findByKeycloakUserId(String keycloakUserId);

    void deleteByKeycloakUserId(String keycloakUserId);
}
