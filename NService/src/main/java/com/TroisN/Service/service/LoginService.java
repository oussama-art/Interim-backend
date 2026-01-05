package com.TroisN.Service.service;

import com.TroisN.Service.dto.auth.TokenResponse;
import com.TroisN.Service.dto.user.LoginRequest;
import com.TroisN.Service.entity.Admin;
import com.TroisN.Service.entity.Candidate;
import com.TroisN.Service.entity.ClientCompany;
import com.TroisN.Service.enums.RoleType;
import com.TroisN.Service.repository.AdminRepository;
import com.TroisN.Service.repository.ClientRepository;
import com.TroisN.Service.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.oauth2.jwt.Jwt;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final ClientRepository clientRepository;
    private final AdminRepository adminRepository;
    private final CandidateRepository candidateRepository;
    private final WebClient webClient;
    private final KeycloakUserService keycloakUserService;

    @Value("${keycloak-client.token-url}")
    private String tokenUrl;

    @Value("${keycloak-client.client-id}")
    private String clientId;

    @Value("${keycloak-client.client-secret}")
    private String clientSecret;


    /**
     * LOGIN user via PASSWORD GRANT
     */
    public TokenResponse login(LoginRequest request) {
        try {
            return webClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("grant_type", "password")
                            .with("client_id", clientId)
                            .with("client_secret", clientSecret)
                            .with("username", request.getEmailAddress())
                            .with("password", request.getPassword())
                    )
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .map(body -> new ResponseStatusException(
                                            HttpStatus.UNAUTHORIZED,
                                            "Authentication failed"
                                    ))
                    )
                    .bodyToMono(TokenResponse.class)
                    .block();

        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication failed"
            );
        }
    }


    public TokenResponse refreshToken(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token is missing"
            );
        }

        try {
            return webClient.post()
                    .uri(tokenUrl)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(
                            BodyInserters.fromFormData("grant_type", "refresh_token")
                                    .with("refresh_token", refreshToken)
                                    .with("client_id", clientId)
                                    .with("client_secret", clientSecret)
                    )
                    .retrieve()
                    .bodyToMono(TokenResponse.class)
                    .block();

        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token expired or invalid"
            );
        }
    }

    /**
     * Finalisation de l'utilisateur Google après login Keycloak
     */
    public void completeGoogleLogin(Jwt jwt, RoleType role) {

        String keycloakUserId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email introuvable dans le token Keycloak"
            );
        }

        switch (role) {

            case CANDIDATE -> {
                if (!candidateRepository.existsByEmailAddress(email)) {
                    Candidate candidate = new Candidate();
                    candidate.setEmailAddress(email);
                    candidate.setKeycloakUserId(keycloakUserId);
                    candidate.setExperienceYear(0);
                    candidateRepository.save(candidate);
                }
                keycloakUserService.assignRoleToExistingUser(email, "CANDIDATE");
            }

            case CLIENT_COMPANY -> {
                if (!clientRepository.existsByEmailAddress(email)) {
                    ClientCompany client = new ClientCompany();
                    client.setEmailAddress(email);
                    client.setKeycloakUserId(keycloakUserId);
                    client.setExperienceYear(0);
                    clientRepository.save(client);
                }
                keycloakUserService.assignRoleToExistingUser(email, "CLIENT");
            }

            case ADMIN -> {
                if (!adminRepository.existsByEmailAddress(email)) {
                    Admin admin = new Admin();
                    admin.setEmailAddress(email);
                    admin.setKeycloakUserId(keycloakUserId);
                    admin.setExperienceYear(0);
                    adminRepository.save(admin);
                }
                keycloakUserService.assignRoleToExistingUser(email, "ADMIN");
            }

            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported role"
            );
        }
    }







}
