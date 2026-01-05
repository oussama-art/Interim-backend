package com.TroisN.Service.service;
import com.TroisN.Service.dto.client.ClientCreateRequest;
import com.TroisN.Service.dto.client.ClientPatchRequest;
import com.TroisN.Service.dto.client.ClientResponse;
import com.TroisN.Service.entity.ClientCompany;
import com.TroisN.Service.mapper.ClientMapper;
import com.TroisN.Service.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;



@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final KeycloakUserService keycloakUserService;


    public ClientService(ClientRepository clientRepository,KeycloakUserService keycloakUserService){
        this.clientRepository = clientRepository;
        this.keycloakUserService = keycloakUserService;

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

            ClientCompany saved = clientRepository.save(client);
            return ClientMapper.toResponseDto(saved);

        } catch (IllegalStateException ex) {

            String message = ex.getMessage() != null ? ex.getMessage() : "";

            if (message.contains("User exists with same email")) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A user with this email already exists"
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error while creating user account"
            );
        }
    }


    //delete a client
    public void deleteClient(Authentication authentication) {

        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        String keycloakUserId = token.getToken().getSubject();

        if (!clientRepository.existsByKeycloakUserId(keycloakUserId)) {
            throw new EntityNotFoundException("Client introuvable");
        }

        clientRepository.deleteByKeycloakUserId(keycloakUserId);
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





}
