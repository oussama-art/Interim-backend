package com.TroisN.Service.service;
import com.TroisN.Service.dto.client.ClientCreateRequest;
import com.TroisN.Service.dto.client.ClientPatchRequest;
import com.TroisN.Service.dto.client.ClientResponse;
import com.TroisN.Service.entity.Account;
import com.TroisN.Service.entity.ClientCompany;
import com.TroisN.Service.enums.RequestStatus;
import com.TroisN.Service.mapper.ClientMapper;
import com.TroisN.Service.repository.AccountRepository;
import com.TroisN.Service.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;


@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final KeycloakUserService keycloakUserService;
    private final AccountRepository accountRepository;


    public ClientService(ClientRepository clientRepository,KeycloakUserService keycloakUserService,
                         AccountRepository accountRepository){
        this.clientRepository = clientRepository;
        this.keycloakUserService = keycloakUserService;
        this.accountRepository = accountRepository;

    }

    public ClientResponse getClientById(Long id) {

        ClientCompany client = clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Client not found with id = " + id
                ));

        return ClientMapper.toResponseDto(client);

    }


    public Page<ClientResponse> getAllClients(int page, int size){
        Pageable pageable = PageRequest.of(page,size);

        return clientRepository.findAll(pageable)
                .map(ClientMapper::toResponseDto);
    }

    public ClientResponse createClient(ClientCreateRequest dto) {

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Passwords do not match"
            );
        }


        if (clientRepository.existsByEmailAddress(dto.getEmailAddress())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Client already exists with this email"
            );
        }

        try {

            String keycloakUserId = keycloakUserService.createClientUser(dto);

            ClientCompany client = new ClientCompany();
            client.setFirstName(dto.getFirstName());
            client.setLastName(dto.getLastName());
            client.setPhoneNumber(dto.getPhoneNumber());
            client.setEmailAddress(dto.getEmailAddress());
            client.setExperienceYear(dto.getExperienceYear());
            client.setDescription(dto.getDescription());
            client.setTitle(dto.getTitle());
            client.setSector(dto.getSector());
            client.setNbEmployee(dto.getNbEmployee());
            client.setKeycloakUserId(keycloakUserId);
            client.setNumDemande(dto.getNumDemande());

            ClientCompany saved = clientRepository.save(client);
            return ClientMapper.toResponseDto(saved);

        } catch (IllegalStateException ex) {

            String message = ex.getMessage() != null ? ex.getMessage() : "";

            if (message.contains("User exists with same email")) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A user with this email already exists in Keycloak"
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error while creating user account"
            );
        }
    }


    @Transactional
    public void deleteClient(Long id) {
        ClientCompany client = clientRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Client introuvable avec l'id : " + id)
                );

        if (client.getKeycloakUserId() != null) {
            try {
                keycloakUserService.deleteUser(client.getKeycloakUserId());
            } catch (Exception e) {
                // Log l'erreur mais continuer la suppression
                System.err.println(
                        "Erreur lors de la suppression de l'utilisateur Keycloak : " + e.getMessage()
                );
            }
        }
        clientRepository.delete(client);
    }



    public ClientResponse patchClient( ClientPatchRequest dto,
                                       Authentication authentication
    ) {

        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        String keycloakUserId = token.getToken().getSubject();

        ClientCompany client = clientRepository.findBykeycloakUserId(keycloakUserId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Client non trouvé")
                );

        if (dto.getEmailAddress() != null)
            client.setEmailAddress(dto.getEmailAddress());

        if (dto.getPhoneNumber() != null)
            client.setPhoneNumber(dto.getPhoneNumber());

        if (dto.getExperienceYear() != null)
            client.setExperienceYear(dto.getExperienceYear());

        if (dto.getDescription() != null)
            client.setDescription(dto.getDescription());

        if (dto.getTitle() != null)
            client.setTitle(dto.getTitle());

        if (dto.getSector() != null)
            client.setSector(dto.getSector());

        if (dto.getNbEmployee() != null)
            client.setNbEmployee(dto.getNbEmployee());

        ClientCompany updated = clientRepository.save(client);
        return ClientMapper.toResponseDto(updated);
    }

    public ClientResponse getClientFromKeycloak(Authentication authentication){

        Jwt jwt = (Jwt) authentication.getPrincipal();

        String keycloakId = jwt.getSubject();

        ClientCompany client = clientRepository.findBykeycloakUserId(keycloakId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        return ClientMapper.toResponseDto(client);
    }

    public List<ClientResponse> getClientsByApprovedAccountRequest(Long accountRequestId) {

        Account account = accountRepository.findById(accountRequestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Account creation request not found"
                ));

        if (account.getStatus() != RequestStatus.APPROVED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Account request is not approved"
            );
        }

        List<ClientCompany> clients =
                clientRepository.findByNumDemande(accountRequestId);

        return clients.stream()
                .map(ClientMapper::toResponseDto)
                .toList();
    }





}
