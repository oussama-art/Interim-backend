package com.TroisN.Service.controller;

import com.TroisN.Service.dto.client.ClientCreateRequest;
import com.TroisN.Service.dto.client.ClientPatchRequest;
import com.TroisN.Service.dto.client.ClientResponse;
import com.TroisN.Service.dto.demande.DemandeResponse;
import com.TroisN.Service.service.ClientService;
import com.TroisN.Service.service.DemandeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.io.IOException;
import java.util.List;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;




@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;
    private final DemandeService demandeService;

    public ClientController(ClientService clientService,DemandeService demandeService){
        this.clientService = clientService;
        this.demandeService = demandeService;
    }

    @PostMapping("/create")
    public ClientResponse createClient(
            @Valid @RequestBody ClientCreateRequest dto
    ) throws IOException {
        return clientService.createClient(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/page")
    public Page<ClientResponse> getAllClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
    {
        return clientService.getAllClients(page,size);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
    }


    @PatchMapping("/me")
    public ClientResponse updateClientInfo  (@Valid @RequestBody ClientPatchRequest dto,
        Authentication authentication
    )
    {
        return clientService.patchClient(dto, authentication);
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/my-demandes")
    public Page<DemandeResponse> getDemandesByClient(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String keycloakId = jwt.getSubject();
        Pageable pageable = PageRequest.of(page, size);
        return demandeService.getDemandesByClient(keycloakId, pageable);
    }



    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/me")
    public ClientResponse getAuthenticatedClient(Authentication authentication){
        return clientService.getClientFromKeycloak(authentication);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/by-account-request/{accountRequestId}")
    public List<ClientResponse> getClientsByAccountRequest(
            @PathVariable Long accountRequestId
    ) {
        return clientService.getClientsByApprovedAccountRequest(accountRequestId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ClientResponse getClientById(@PathVariable Long id) {
        return clientService.getClientById(id);
    }




}
