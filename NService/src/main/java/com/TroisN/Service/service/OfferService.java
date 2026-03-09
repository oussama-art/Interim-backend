package com.TroisN.Service.service;

import com.TroisN.Service.dto.assignment.AssignmentResponse;
import com.TroisN.Service.dto.offer.OfferCreateRequest;
import com.TroisN.Service.dto.offer.OfferResponse;
import com.TroisN.Service.dto.offer.offreCandidate.OfferAddCandidatesRequest;
import com.TroisN.Service.entity.*;
import com.TroisN.Service.enums.CanidateStatus;
import com.TroisN.Service.enums.DemandeStatus;
import com.TroisN.Service.enums.OfferCandidateStatus;
import com.TroisN.Service.mapper.AssignmentMapper;
import com.TroisN.Service.mapper.OfferMapper;
import com.TroisN.Service.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class OfferService {

    private final OfferRepository offerRepository;
    private final DemandeRepository demandeRepository;
    private final CandidateRepository candidateRepository;
    private final AssignmentRepository assignmentRepository;
    private final OfferCandidateRepository offerCandidateRepository;

    public OfferService(
            OfferRepository offerRepository,
            DemandeRepository demandeRepository,
            CandidateRepository candidateRepository,
            AssignmentRepository assignmentRepository,
            OfferCandidateRepository offerCandidateRepository
    ) {
        this.offerRepository = offerRepository;
        this.demandeRepository = demandeRepository;
        this.candidateRepository = candidateRepository;
        this.assignmentRepository = assignmentRepository;
        this.offerCandidateRepository = offerCandidateRepository;

    }
//
//    @Transactional
//    public OfferResponse createOffer(Long clientId, OfferCreateRequest request) {
//
//        Demande demande = demandeRepository.findById(request.getDemandeId())
//                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));
//
//        assertDemandeIsOpen(demande);
//
//        if (!demande.getClient().getId().equals(clientId)) {
//            throw new IllegalStateException("Cette demande n'appartient pas au client");
//        }
//
//        LocalDate demandeStart = demande.getStartDate();
//        LocalDate demandeEnd = demande.getEndDate();
//
//        Offer offer = new Offer();
//        offer.setDemande(demande);
//        offer.setClient(demande.getClient());
//
//        List<OfferCandidate> offerCandidates = request.getProfilsCandidates()
//                .entrySet()
//                .stream()
//                .flatMap(entry -> {
//
//                    Long profilId = entry.getKey();
//                    List<Long> candidateIds = entry.getValue();
//
//                    DemandeProfil demandeProfil = demande.getProfils()
//                            .stream()
//                            .filter(p -> p.getId().equals(profilId))
//                            .findFirst()
//                            .orElseThrow(() ->
//                                    new IllegalArgumentException(
//                                            "Profil ID = " + profilId + " non associé à cette demande"
//                                    )
//                            );
//
//                    return candidateIds.stream().map(candidateId -> {
//
//                        Candidate candidate = candidateRepository.findById(candidateId)
//                                .orElseThrow(() ->
//                                        new IllegalArgumentException(
//                                                "Candidat introuvable ID = " + candidateId
//                                        )
//                                );
//
//
//                        switch (candidate.getStatus()) {
//
//                            case AVAILABLE -> {
//                                // OK
//                            }
//
//                            case ON_MISSION -> {
//
//                                List<Assignment> overlappingAssignments =
//                                        assignmentRepository.findOverlappingAssignments(
//                                                candidate.getId(),
//                                                demandeStart,
//                                                demandeEnd
//                                        );
//
//                                if (!overlappingAssignments.isEmpty()) {
//                                    Assignment current = overlappingAssignments.get(0);
//
//                                    throw new IllegalStateException(
//                                            "Le candidat ID = " + candidateId +
//                                                    " est en mission jusqu’au " + current.getEndDate() +
//                                                    " (chevauchement avec la période demandée)"
//                                    );
//                                }
//                            }
//
//                            case INACTIVE -> throw new IllegalStateException(
//                                    "Le candidat ID = " + candidateId + " est inactif"
//                            );
//
//                            default -> throw new IllegalStateException(
//                                    "Statut inconnu pour le candidat ID = " + candidateId
//                            );
//                        }
//
//                        OfferCandidate oc = new OfferCandidate();
//                        oc.setOffer(offer);
//                        oc.setCandidate(candidate);
//                        oc.setDemandeProfil(demandeProfil);
//                        // status = PROPOSED (via @PrePersist)
//
//                        return oc;
//                    });
//                })
//                .toList();
//
//        offer.setProposedCandidates(offerCandidates);
//
//
//        Offer savedOffer = offerRepository.save(offer);
//
//        return OfferMapper.toOfferResponse(savedOffer);
//    }


    @Transactional
    public OfferResponse createOffer(Long clientId, OfferCreateRequest request) {

        Demande demande = demandeRepository.findById(request.getDemandeId())
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));

        assertDemandeIsOpen(demande);

        if (!demande.getClient().getId().equals(clientId)) {
            throw new IllegalStateException("Cette demande n'appartient pas au client");
        }

        LocalDate demandeStart = demande.getStartDate();
        LocalDate demandeEnd = demande.getEndDate();

        Offer offer = new Offer();
        offer.setDemande(demande);
        offer.setClient(demande.getClient());

        List<OfferCandidate> offerCandidates = request.getProfilsCandidates()
                .entrySet()
                .stream()
                .flatMap(entry -> {

                    Long profilId = entry.getKey();
                    List<Long> candidateIds = entry.getValue();

                    DemandeProfil demandeProfil = demande.getProfils()
                            .stream()
                            .filter(p -> p.getId().equals(profilId))
                            .findFirst()
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Profil ID = " + profilId + " non associé à cette demande"
                                    )
                            );

                    return candidateIds.stream().map(candidateId -> {

                        Candidate candidate = candidateRepository.findById(candidateId)
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Candidat introuvable ID = " + candidateId
                                        )
                                );

                        // 🔎 Vérification statut candidat
                        switch (candidate.getStatus()) {

                            case AVAILABLE -> {
                                // OK
                            }

                            case ON_MISSION -> {

                                List<Assignment> overlappingAssignments =
                                        assignmentRepository.findOverlappingAssignments(
                                                candidate.getId(),
                                                demandeStart,
                                                demandeEnd
                                        );

                                if (!overlappingAssignments.isEmpty()) {
                                    Assignment current = overlappingAssignments.get(0);

                                    throw new IllegalStateException(
                                            "Le candidat ID = " + candidateId +
                                                    " est en mission jusqu’au " + current.getEndDate() +
                                                    " (chevauchement avec la période demandée)"
                                    );
                                }
                            }

                            case INACTIVE -> throw new IllegalStateException(
                                    "Le candidat ID = " + candidateId + " est inactif"
                            );

                            default -> throw new IllegalStateException(
                                    "Statut inconnu pour le candidat ID = " + candidateId
                            );
                        }

                        // 🔎 Vérification historique offres pour cette demande
                        List<OfferCandidate> previous =
                                offerCandidateRepository
                                        .findByCandidate_IdAndOffer_Demande_Id(
                                                candidateId,
                                                demande.getId()
                                        );

                        for (OfferCandidate existing : previous) {

                            if (existing.getStatus() == OfferCandidateStatus.PROPOSED) {
                                throw new IllegalStateException(
                                        "Le candidat ID = " + candidateId +
                                                " est déjà proposé pour cette demande"
                                );
                            }

                            if (existing.getStatus() == OfferCandidateStatus.ACCEPTED) {
                                throw new IllegalStateException(
                                        "Le candidat ID = " + candidateId +
                                                " a déjà été accepté pour cette demande"
                                );
                            }

                            // Si REJECTED → on autorise
                        }

                        // Création nouvelle proposition
                        OfferCandidate oc = new OfferCandidate();
                        oc.setOffer(offer);
                        oc.setCandidate(candidate);
                        oc.setDemandeProfil(demandeProfil);
                        // status = PROPOSED via @PrePersist

                        return oc;
                    });
                })
                .toList();

        offer.setProposedCandidates(offerCandidates);

        Offer savedOffer = offerRepository.save(offer);

        return OfferMapper.toOfferResponse(savedOffer);
    }


    @Transactional
    public OfferResponse addCandidatesToOffer(
            Long offerId,
            Long clientId,
            OfferAddCandidatesRequest request
    ) {

        Offer offer = offerRepository.findOfferWithDetails(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offre introuvable"));

        if (!offer.getClient().getId().equals(clientId)) {
            throw new IllegalStateException("Cette offre n'appartient pas au client");
        }

        Demande demande = offer.getDemande();
        assertDemandeIsOpen(demande);

        LocalDate demandeStart = demande.getStartDate();
        LocalDate demandeEnd = demande.getEndDate();

        DemandeProfil demandeProfil = demande.getProfils()
                .stream()
                .filter(p -> p.getId().equals(request.getDemandeProfilId()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Profil non associé à la demande")
                );

        List<OfferCandidate> offerCandidates = offer.getProposedCandidates();

        for (Long candidateId : request.getCandidateIds()) {

            Candidate candidate = candidateRepository.findById(candidateId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Candidat introuvable ID = " + candidateId
                            )
                    );

            // 🔎 Vérifier si déjà présent dans cette offre pour ce profil
            OfferCandidate existing = offerCandidates.stream()
                    .filter(oc ->
                            oc.getCandidate().getId().equals(candidateId)
                                    && oc.getDemandeProfil().getId().equals(demandeProfil.getId())
                    )
                    .findFirst()
                    .orElse(null);

            if (existing != null) {

                if (existing.getStatus() == OfferCandidateStatus.PROPOSED) {
                    throw new IllegalStateException(
                            "Le candidat ID = " + candidateId + " est déjà proposé"
                    );
                }

                if (existing.getStatus() == OfferCandidateStatus.ACCEPTED) {
                    throw new IllegalStateException(
                            "Le candidat ID = " + candidateId + " est déjà accepté"
                    );
                }

                if (existing.getStatus() == OfferCandidateStatus.REJECTED) {
                    // 🔁 On le repropose
                    existing.setStatus(OfferCandidateStatus.PROPOSED);
                    continue;
                }
            }

            // 🔎 Vérifier statut métier du candidat
            switch (candidate.getStatus()) {

                case AVAILABLE -> {
                    // OK
                }

                case ON_MISSION -> {
                    List<Assignment> overlappingAssignments =
                            assignmentRepository.findOverlappingAssignments(
                                    candidate.getId(),
                                    demandeStart,
                                    demandeEnd
                            );

                    if (!overlappingAssignments.isEmpty()) {
                        Assignment current = overlappingAssignments.get(0);
                        throw new IllegalStateException(
                                "Le candidat ID = " + candidateId +
                                        " est en mission jusqu’au " + current.getEndDate()
                        );
                    }
                }

                case INACTIVE -> throw new IllegalStateException(
                        "Le candidat ID = " + candidateId + " est inactif"
                );

                default -> throw new IllegalStateException(
                        "Statut inconnu pour le candidat ID = " + candidateId
                );
            }

            // ➕ Nouveau candidat
            OfferCandidate newCandidate = new OfferCandidate();
            newCandidate.setOffer(offer);
            newCandidate.setCandidate(candidate);
            newCandidate.setDemandeProfil(demandeProfil);

            offerCandidates.add(newCandidate);
        }

        Offer saved = offerRepository.save(offer);

        return OfferMapper.toOfferResponse(saved);
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

        assertDemandeIsOpen(offer.getDemande());

        OfferCandidate accepted = null;

        for (OfferCandidate oc : offer.getProposedCandidates()) {

            Candidate candidate = oc.getCandidate();

            if (candidate.getId().equals(candidateId)) {

                boolean hasOverlap = assignmentRepository.existsOverlappingAssignment(
                        candidateId,
                        startDate,
                        endDate
                );

                if (hasOverlap) {
                    throw new IllegalStateException(
                            "Le candidat a déjà une mission sur cette période. " +
                                    "Prochaine disponibilité estimée : " + candidate.getNextAvailableDate()
                    );
                }


                oc.setStatus(OfferCandidateStatus.ACCEPTED);

                candidate.setNextAvailableDate(endDate);

                accepted = oc;
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

    @Transactional(readOnly = true)
    public List<OfferResponse> getAllOffers() {

        List<Offer> offers = offerRepository.findAllWithDetails();

        return offers.stream()
                .map(OfferMapper::toOfferResponse)
                .toList();
    }


    @Transactional
    public void deleteOfferByAdmin(Long offerId) {

        Offer offer = offerRepository.findOfferWithDetails(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offre introuvable"));


        boolean accepted = offer.getProposedCandidates().stream()
                .anyMatch(oc -> oc.getStatus() == OfferCandidateStatus.ACCEPTED);

        if (accepted) {
            throw new IllegalStateException(
                    "Impossible de supprimer une offre déjà acceptée"
            );
        }

        offerRepository.delete(offer);
    }

    @Transactional(readOnly = true)
    public List<OfferResponse> getOffersByDemandeId(Long demandeId) {

        List<Offer> offers = offerRepository.findByDemande_Id(demandeId);

        return offers.stream()
                .map(OfferMapper::toOfferResponse)
                .toList();
    }

    @Transactional
    public void rejectCandidate(
            Long offerId,
            Long candidateId,
            Long clientId
    ) {

        Offer offer = offerRepository.findOfferWithDetails(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offre introuvable"));

        assertDemandeIsOpen(offer.getDemande());

        if (!offer.getClient().getId().equals(clientId)) {
            throw new IllegalStateException("Cette offre n'appartient pas au client");
        }

        OfferCandidate target = offer.getProposedCandidates()
                .stream()
                .filter(oc -> oc.getCandidate().getId().equals(candidateId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Le candidat ne fait pas partie de cette offre")
                );

        if (target.getStatus() != OfferCandidateStatus.PROPOSED) {
            throw new IllegalStateException(
                    "Seuls les candidats PROPOSED peuvent être rejetés"
            );
        }

        target.setStatus(OfferCandidateStatus.REJECTED);
    }

    private void assertDemandeIsOpen(Demande demande) {
        if (demande.getStatus() == DemandeStatus.CLOSED) {
            throw new IllegalStateException(
                    "Cette demande est clôturée. Aucune action n’est autorisée."
            );
        }
    }






}
