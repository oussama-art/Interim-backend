package com.TroisN.Service.service;
import com.TroisN.Service.dto.demande.DemandeRequest;
import com.TroisN.Service.dto.demande.DemandeResponse;
import com.TroisN.Service.entity.ClientCompany;
import com.TroisN.Service.entity.Demande;
import com.TroisN.Service.entity.DemandeProfil;
import com.TroisN.Service.mapper.DemandeMapper;
import com.TroisN.Service.repository.AdminRepository;
import com.TroisN.Service.repository.ClientRepository;
import com.TroisN.Service.repository.DemandeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DemandeService {

    private final DemandeRepository demandeRepository;
    private final ClientRepository clientRepository;
    private final AdminService adminService;

    public DemandeResponse createDemande(Authentication authentication,
                                         DemandeRequest request) {

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
        return DemandeMapper.toDemandeResponse(saved);
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


    public DemandeResponse getDemandeByIdAndClient(Authentication authentication, Long demandeId) {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = jwt.getSubject();

        ClientCompany client = clientRepository.findBykeycloakUserId(keycloakId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        Demande demande = demandeRepository.findByIdAndClientId(demandeId, client.getId())
                .orElseThrow(() -> new IllegalArgumentException("Demande not found for this client"));

        return DemandeMapper.toDemandeResponse(demande);
    }



    public void deleteDemande(Long demandeId) {
        if (!demandeRepository.existsById(demandeId)) {
            throw new IllegalArgumentException("Demande introuvable avec ID = " + demandeId);
        }

        demandeRepository.deleteById(demandeId);
    }

    public DemandeResponse updateDemande(Authentication authentication,
                                         Long demandeId,
                                         DemandeRequest request) {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = jwt.getSubject();

        ClientCompany client = clientRepository.findBykeycloakUserId(keycloakId)
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable"));

        // Vérifier que la demande appartient au client
        Demande demande = demandeRepository
                .findByIdAndClientId(demandeId, client.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Demande introuvable pour ce client"
                ));

        // Mise à jour des champs simples
        demande.setTitle(request.getTitle());
        demande.setDescription(request.getDescription());
        demande.setTotalEmployeesNeeded(request.getTotalEmployeesNeeded());


        demande.getProfils().clear();

        request.getProfils().forEach(p -> {
            DemandeProfil profil = new DemandeProfil();
            profil.setProfilName(p.getProfilName());
            profil.setQuantity(p.getQuantity());
            profil.setDemande(demande);
            demande.getProfils().add(profil);
        });

        Demande saved = demandeRepository.save(demande);

        return DemandeMapper.toDemandeResponse(saved);
    }



}
