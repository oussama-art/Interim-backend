package com.TroisN.Service.service;

import com.TroisN.Service.dto.assignment.AssignmentResponse;
import com.TroisN.Service.dto.offer.OfferCreateRequest;
import com.TroisN.Service.dto.offer.OfferResponse;
import com.TroisN.Service.entity.*;
import com.TroisN.Service.enums.CanidateStatus;
import com.TroisN.Service.enums.OfferCandidateStatus;
import com.TroisN.Service.mapper.AssignmentMapper;
import com.TroisN.Service.mapper.OfferMapper;
import com.TroisN.Service.repository.AssignmentRepository;
import com.TroisN.Service.repository.CandidateRepository;
import com.TroisN.Service.repository.DemandeRepository;
import com.TroisN.Service.repository.OfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class OfferService {

    private final OfferRepository offerRepository;
    private final DemandeRepository demandeRepository;
    private final CandidateRepository candidateRepository;
    private final AssignmentRepository assignmentRepository;

    public OfferService(
            OfferRepository offerRepository,
            DemandeRepository demandeRepository,
            CandidateRepository candidateRepository,
            AssignmentRepository assignmentRepository
    ) {
        this.offerRepository = offerRepository;
        this.demandeRepository = demandeRepository;
        this.candidateRepository = candidateRepository;
        this.assignmentRepository = assignmentRepository;

    }


    public OfferResponse createOffer(Long clientId, OfferCreateRequest request) {

        Demande demande = demandeRepository.findById(request.getDemandeId())
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));

        if (!demande.getClient().getId().equals(clientId)) {
            throw new IllegalStateException("Cette demande n'appartient pas au client");
        }

        Offer offer = new Offer();
        offer.setDemande(demande);
        offer.setClient(demande.getClient());

        List<OfferCandidate> offerCandidates = request.getCandidateIds()
                .stream()
                .map(candidateId -> {

                    Candidate candidate = candidateRepository.findById(candidateId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException("Candidat introuvable ID = " + candidateId));


                    switch (candidate.getStatus()) {
                        case AVAILABLE -> {
                            // OK → on continue
                        }
                        case ON_MISSION -> throw new IllegalStateException(
                                "Le candidat ID = " + candidateId +
                                        " est actuellement en mission"
                        );
                        case INACTIVE -> throw new IllegalStateException(
                                "Le candidat ID = " + candidateId +
                                        " est inactif"
                        );
                        default -> throw new IllegalStateException(
                                "Statut inconnu pour le candidat ID = " + candidateId
                        );
                    }

                    OfferCandidate oc = new OfferCandidate();
                    oc.setOffer(offer);
                    oc.setCandidate(candidate);
                    return oc;
                })
                .toList();

        offer.setProposedCandidates(offerCandidates);

        return OfferMapper.toOfferResponse(
                offerRepository.save(offer)
        );
    }

    @Transactional(readOnly = true)
    public OfferResponse getOfferById(Long offerId) {

        Offer offer = offerRepository.findOfferWithDetails(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offre introuvable"));

        return OfferMapper.toOfferResponse(offer);
    }

    @Transactional
    public AssignmentResponse acceptOffer(
            Long offerId,
            Long candidateId,
            Long clientId,
            LocalDate startDate,
            LocalDate endDate
    ) {

        Offer offer = offerRepository.findOfferWithDetails(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offre introuvable"));

        if (!offer.getClient().getId().equals(clientId)) {
            throw new IllegalStateException("Cette offre n'appartient pas au client");
        }

        OfferCandidate accepted = null;

        for (OfferCandidate oc : offer.getProposedCandidates()) {

            Candidate candidate = oc.getCandidate();

            if (candidate.getId().equals(candidateId)) {

                if (candidate.getStatus() != CanidateStatus.AVAILABLE) {
                    throw new IllegalStateException(
                            "Le candidat n'est pas disponible (statut = " + candidate.getStatus() + ")"
                    );
                }

                oc.setStatus(OfferCandidateStatus.ACCEPTED);
                candidate.setStatus(CanidateStatus.ON_MISSION);
                accepted = oc;

            } else {
                oc.setStatus(OfferCandidateStatus.REJECTED);
            }
        }

        if (accepted == null) {
            throw new IllegalArgumentException(
                    "Le candidat ne fait pas partie de cette offre"
            );
        }

        Assignment assignment = new Assignment();
        assignment.setCandidate(accepted.getCandidate());
        assignment.setDemande(offer.getDemande());
        assignment.setClient(offer.getClient());
        assignment.setStartDate(startDate);
        assignment.setEndDate(endDate);

        Assignment saved = assignmentRepository.save(assignment);

        return AssignmentMapper.toResponse(saved);
    }



    @Transactional(readOnly = true)
    public List<OfferResponse> getOffersByClientId(Long clientId) {

        List<Offer> offers = offerRepository.findOffersByClientIdWithDetails(clientId);

        return offers.stream()
                .map(OfferMapper::toOfferResponse)
                .toList();
    }


}
