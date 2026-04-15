package com.TroisN.Service.service;
import com.TroisN.Service.dto.demande.DemandeRequest;
import com.TroisN.Service.dto.demande.DemandeResponse;
import com.TroisN.Service.entity.*;
import com.TroisN.Service.enums.DemandeStatus;
import com.TroisN.Service.enums.NotificationRecipientType;
import com.TroisN.Service.enums.OfferCandidateStatus;
import com.TroisN.Service.event.DemandeDeletedEvent;
import com.TroisN.Service.mapper.DemandeMapper;
import com.TroisN.Service.repository.AdminRepository;
import com.TroisN.Service.repository.AssignmentRepository;
import com.TroisN.Service.repository.ClientRepository;
import com.TroisN.Service.repository.DemandeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DemandeService {

    private final DemandeRepository demandeRepository;
    private final ClientRepository clientRepository;
    private final AdminService adminService;
    private final AssignmentRepository assignmentRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;


    public DemandeResponse createDemande(Authentication authentication,
                                         DemandeRequest request) {

        validatePeriod(request);

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = jwt.getSubject();

        ClientCompany client = clientRepository.findBykeycloakUserId(keycloakId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Client introuvable pour l'utilisateur connecté"
                ));

        Demande demande = DemandeMapper.toDemandeEntity(request);
        demande.setClient(client);

        Demande saved = demandeRepository.save(demande);

        adminService.notifyAdmins(saved);

        String clientName = buildClientName(client);

        notificationService.createAndPublish(
                "DEMANDE_CREATED",
                "Nouvelle demande",
                "Une nouvelle demande a été ajoutée par le client : " + clientName,
                NotificationRecipientType.ADMIN_TOPIC,
                "ADMIN_DEMANDES",
                "/admin/demandes/" + saved.getId(),
                saved.getId(),
                "DEMANDE"
        );

        return DemandeMapper.toDemandeResponse(saved);
    }

    private String buildClientName(ClientCompany client) {

        String firstName = client.getFirstName() != null ? client.getFirstName() : "";
        String lastName = client.getLastName() != null ? client.getLastName() : "";

        String fullName = (firstName + " " + lastName).trim();

        if (!fullName.isEmpty()) {
            return fullName;
        }

        return client.getEmailAddress();
    }




    @Transactional(readOnly = true)
    public Page<DemandeResponse> getAllDemandes(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Demande> demandes = demandeRepository.findAllWithProfils(pageable);
        return demandes.map(DemandeMapper::toDemandeResponse);
    }


    @Transactional(readOnly = true)
    public Page<DemandeResponse> getDemandesByClient(String keycloakId, Pageable pageable) {

        ClientCompany client = clientRepository.findBykeycloakUserId(keycloakId)
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable"));

        return demandeRepository.findByClient_Id(client.getId(), pageable)
                .map(DemandeMapper::toDemandeResponse);
    }

    public DemandeResponse getDemandeByIdAndClient(
            Authentication authentication,
            Long demandeId
    ) {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = jwt.getSubject();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Demande demande;

        if (isAdmin) {
            demande = demandeRepository.findById(demandeId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Demande introuvable"
                    ));
        } else {
            ClientCompany client = clientRepository.findBykeycloakUserId(keycloakId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Client introuvable"
                    ));

            demande = demandeRepository.findByIdAndClientId(demandeId, client.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Accès refusé ou demande introuvable"
                    ));
        }

        return DemandeMapper.toDemandeResponse(demande);
    }


    public DemandeResponse updateDemande(
            Authentication authentication,
            Long demandeId,
            DemandeRequest request
    ) {

        validatePeriod(request);

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = jwt.getSubject();

        ClientCompany client = clientRepository.findBykeycloakUserId(keycloakId)
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable"));

        Demande demande = demandeRepository
                .findByIdAndClientId(demandeId, client.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Demande introuvable pour ce client"
                ));

        if (demande.getStatus() == DemandeStatus.CLOSED) {
            throw new IllegalStateException(
                    "Impossible de modifier une demande déjà clôturée"
            );
        }

        demande.setTitle(request.getTitle());
        demande.setDescription(request.getDescription());
        demande.setTotalEmployeesNeeded(request.getTotalEmployeesNeeded());
        demande.setStartDate(request.getStartDate());
        demande.setEndDate(request.getEndDate());

        demande.getProfils().clear();

        request.getProfils().forEach(p -> {
            DemandeProfil profil = new DemandeProfil();
            profil.setProfilName(p.getProfilName());
            profil.setQuantity(p.getQuantity());
            profil.setDemande(demande);
            demande.getProfils().add(profil);
        });

        Demande saved = demandeRepository.save(demande);

        notificationService.createAndPublish(
                "DEMANDE_UPDATED",
                "Demande modifiée",
                "La demande \"" + saved.getTitle() + "\" a été modifiée par le client : " + buildClientName(client),
                NotificationRecipientType.ADMIN_TOPIC,
                "ADMIN_DEMANDES",
                "/admin/demandes/" + saved.getId(),
                saved.getId(),
                "DEMANDE"
        );

        return DemandeMapper.toDemandeResponse(saved);
    }



    private void validatePeriod(DemandeRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException(
                    "La date de fin doit être postérieure ou égale à la date de début"
            );
        }
    }

    @Transactional
    public void deleteDemande(Long demandeId) {

        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Demande introuvable avec ID = " + demandeId
                        )
                );

        // récupérer les assignments avant suppression
        List<Assignment> assignments = assignmentRepository.findByDemande_Id(demandeId);

        // suppression
        assignmentRepository.deleteByDemande_Id(demandeId);

        demandeRepository.delete(demande);

        // publier event avec assignments
        eventPublisher.publishEvent(new DemandeDeletedEvent(demandeId, assignments));
    }


    @Transactional
    public DemandeResponse closeDemande(Long demandeId) {

        Demande demande = demandeRepository.findByIdWithOffers(demandeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Demande introuvable avec ID = " + demandeId
                ));

        if (demande.getStatus() == DemandeStatus.CLOSED) {
            throw new IllegalStateException(
                    "Cette demande est déjà clôturée"
            );
        }

        for (Offer offer : demande.getOffers()) {

            offer.getProposedCandidates().forEach(oc -> {
                if (oc.getStatus() == OfferCandidateStatus.PROPOSED) {
                    oc.setStatus(OfferCandidateStatus.REJECTED);
                }
            });
        }

        demande.setStatus(DemandeStatus.CLOSED);

        return DemandeMapper.toDemandeResponse(demande);
    }



}
