package com.TroisN.Service.service;

import com.TroisN.Service.dto.assignment.AssignmentResponse;
import com.TroisN.Service.dto.notification.NotificationMessage;
import com.TroisN.Service.dto.offer.OfferCreateRequest;
import com.TroisN.Service.dto.offer.OfferResponse;
import com.TroisN.Service.dto.offer.offreCandidate.OfferAddCandidatesRequest;
import com.TroisN.Service.entity.*;
import com.TroisN.Service.enums.CanidateStatus;
import com.TroisN.Service.enums.DemandeStatus;
import com.TroisN.Service.enums.NotificationRecipientType;
import com.TroisN.Service.enums.OfferCandidateStatus;
import com.TroisN.Service.mapper.AssignmentMapper;
import com.TroisN.Service.mapper.OfferMapper;
import com.TroisN.Service.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class OfferService {

    private final OfferRepository offerRepository;
    private final DemandeRepository demandeRepository;
    private final CandidateRepository candidateRepository;
    private final AssignmentRepository assignmentRepository;
    private final OfferCandidateRepository offerCandidateRepository;
    private final WebSocketNotificationService webSocketNotificationService;
    private final NotificationService notificationService;

    public OfferService(
            OfferRepository offerRepository,
            DemandeRepository demandeRepository,
            CandidateRepository candidateRepository,
            AssignmentRepository assignmentRepository,
            OfferCandidateRepository offerCandidateRepository,
            WebSocketNotificationService webSocketNotificationService,
            NotificationService notificationService
    ) {
        this.offerRepository = offerRepository;
        this.demandeRepository = demandeRepository;
        this.candidateRepository = candidateRepository;
        this.assignmentRepository = assignmentRepository;
        this.offerCandidateRepository = offerCandidateRepository;
        this.webSocketNotificationService = webSocketNotificationService;
        this.notificationService = notificationService;
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

                        List<OfferCandidate> previous =
                                offerCandidateRepository.findByCandidate_IdAndOffer_Demande_Id(
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
                        }

                        OfferCandidate oc = new OfferCandidate();
                        oc.setOffer(offer);
                        oc.setCandidate(candidate);
                        oc.setDemandeProfil(demandeProfil);

                        return oc;
                    });
                })
                .toList();

        offer.setProposedCandidates(offerCandidates);

        Offer savedOffer = offerRepository.save(offer);

        String demandeReference = savedOffer.getDemande().getReference();

        // IMPORTANT :
        // recipientKey doit être exactement la même valeur que le preferred_username du JWT
        String clientUsername = savedOffer.getClient().getEmailAddress();

        notificationService.createAndPublish(
                "OFFER_CREATED",
                "Nouvelle offre reçue",
                "Une nouvelle offre a été créée pour la demande " + demandeReference + ".",
                NotificationRecipientType.USER_QUEUE,
                clientUsername,
                "/offers",
                savedOffer.getId(),
                "OFFER"
        );

//        System.out.println(" Notification persistée pour le client : " + clientUsername);
//        System.out.println(" Client email : " + clientUsername);
//        System.out.println(" Offer ID : " + savedOffer.getId());

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
                    existing.setStatus(OfferCandidateStatus.PROPOSED);
                    continue;
                }
            }

            switch (candidate.getStatus()) {

                case AVAILABLE -> {
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

            OfferCandidate newCandidate = new OfferCandidate();
            newCandidate.setOffer(offer);
            newCandidate.setCandidate(candidate);
            newCandidate.setDemandeProfil(demandeProfil);

            offerCandidates.add(newCandidate);
        }

        Offer saved = offerRepository.save(offer);

        String clientUsername = saved.getClient().getEmailAddress();
        String demandeReference = saved.getDemande().getReference();

        notificationService.createAndPublish(
                "CANDIDATES_ADDED",
                "Nouveaux candidats proposés",
                "De nouveaux candidats ont été ajoutés à votre offre pour la demande "
                        + demandeReference + ".",
                NotificationRecipientType.USER_QUEUE,
                clientUsername,
                "/offers/" + saved.getId(),
                saved.getId(),
                "OFFER"
        );

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
                candidate.setStatus(CanidateStatus.ON_MISSION);

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

        notificationService.createAndPublish(
                "CANDIDATE_ACCEPTED",
                "Candidat accepté par le client",
                "Le client a accepté le candidat " +
                        accepted.getCandidate().getFirstName() + " " +
                        accepted.getCandidate().getLastName() +
                        " pour l'offre ID " + offer.getId() +
                        " liée à la demande " + offer.getDemande().getReference() + ".",
                NotificationRecipientType.ADMIN_TOPIC,
                "ADMIN_OFFERS",
                "/admin/offers/",
                offer.getId(),
                "OFFER"
        );

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

//        if (accepted) {
//            throw new IllegalStateException(
//                    "Impossible de supprimer une offre déjà acceptée"
//            );
//        }

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

        notificationService.createAndPublish(
                "CANDIDATE_REJECTED",
                "Candidat refusé par le client",
                "Le client a refusé le candidat " +
                        target.getCandidate().getFirstName() + " " +
                        target.getCandidate().getLastName() +
                        " pour l'offre ID " + offer.getId() +
                        " liée à la demande " + offer.getDemande().getReference() + ".",
                NotificationRecipientType.ADMIN_TOPIC,
                "ADMIN_OFFERS",
                "/admin/offers/",
                offer.getId(),
                "OFFER"
        );
    }

    private void assertDemandeIsOpen(Demande demande) {
        if (demande.getStatus() == DemandeStatus.CLOSED) {
            throw new IllegalStateException(
                    "Cette demande est clôturée. Aucune action n’est autorisée."
            );
        }
    }






}
