package com.TroisN.Service.repository;

import com.TroisN.Service.entity.OfferCandidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferCandidateRepository
        extends JpaRepository<OfferCandidate, Long> {

    List<OfferCandidate> findByCandidate_IdAndOffer_Demande_Id(
            Long candidateId,
            Long demandeId
    );
}
