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
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;

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

    /* =====================================================
       LOGIN
       ===================================================== */
    public TokenResponse login(LoginRequest request) {

        checkIfUserAllowedToLogin(request.getEmailAddress());

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

    /* =====================================================
       REFRESH TOKEN
       ===================================================== */
    public TokenResponse refreshToken(String refreshToken) {

        try {
            return webClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(
                            BodyInserters.fromFormData("grant_type", "refresh_token")
                                    .with("refresh_token", refreshToken)
                                    .with("client_id", clientId)
                                    .with("client_secret", clientSecret)
                    )
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .map(body -> new ResponseStatusException(
                                            HttpStatus.UNAUTHORIZED,
                                            "Refresh token expired or invalid"
                                    ))
                    )
                    .bodyToMono(TokenResponse.class)
                    .block();

        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token failed"
            );
        }
    }

    /* =====================================================
       GOOGLE LOGIN
       ===================================================== */
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
//                    candidate.setKeycloakUserId(keycloakUserId);
                    candidate.setExperienceYear(0);
                    candidate.setActive(true);
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
                    client.setActive(true);
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
                    admin.setActive(true);
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

    /* =====================================================
       LOGOUT
       ===================================================== */
    public void logout(String refreshToken) {

        try {
            webClient.post()
                    .uri(tokenUrl.replace("/token", "/logout"))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters
                            .fromFormData("client_id", clientId)
                            .with("client_secret", clientSecret)
                            .with("refresh_token", refreshToken))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

        } catch (Exception e) {
            throw new RuntimeException("Keycloak logout failed", e);
        }
    }

    /* =====================================================
       SUSPEND USER (PERMANENT)
       ===================================================== */
    public void suspendUser(String email) {

        keycloakUserService.disableUserByEmail(email);

//        candidateRepository.findByKeycloakUserId(email)
//                .ifPresent(c -> c.setActive(false));

        clientRepository.findBykeycloakUserId(email)
                .ifPresent(c -> c.setActive(false));

        adminRepository.findByEmailAddress(email)
                .ifPresent(a -> a.setActive(false));
    }

    /* =====================================================
       REACTIVATE USER
       ===================================================== */
    public void reactivateUser(String email) {

        keycloakUserService.enableUserByEmail(email);

//        candidateRepository.findByKeycloakUserId(email)
//                .ifPresent(c -> c.setActive(true));

        clientRepository.findBykeycloakUserId(email)
                .ifPresent(c -> c.setActive(true));

        adminRepository.findByEmailAddress(email)
                .ifPresent(a -> a.setActive(true));
    }

    /* =====================================================
       TEMPORARY SUSPENSION
       ===================================================== */
    public void suspendUserTemporarily(String email, int minutes) {

        LocalDateTime suspendedUntil = LocalDateTime.now().plusMinutes(minutes);

//        candidateRepository.findByKeycloakUserId(email)
//                .ifPresent(c -> c.setSuspendedUntil(suspendedUntil));

        clientRepository.findBykeycloakUserId(email)
                .ifPresent(c -> c.setSuspendedUntil(suspendedUntil));

        adminRepository.findByEmailAddress(email)
                .ifPresent(a -> a.setSuspendedUntil(suspendedUntil));
    }

    /* =====================================================
       CHECK USER STATUS
       ===================================================== */
    private void checkIfUserAllowedToLogin(String email) {

//        candidateRepository.findByKeycloakUserId(email)
//                .ifPresent(this::validateStatus);

        clientRepository.findBykeycloakUserId(email)
                .ifPresent(this::validateStatus);

        adminRepository.findByEmailAddress(email)
                .ifPresent(this::validateStatus);
    }

    private void validateStatus(Object user) {

        if (user instanceof Candidate candidate) {

            if (!candidate.isActive()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Compte suspendu");
            }

            if (candidate.getSuspendedUntil() != null &&
                    candidate.getSuspendedUntil().isAfter(LocalDateTime.now())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Compte temporairement suspendu"
                );
            }
        }

        if (user instanceof ClientCompany client) {

            if (!client.isActive()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Compte suspendu");
            }

            if (client.getSuspendedUntil() != null &&
                    client.getSuspendedUntil().isAfter(LocalDateTime.now())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Compte temporairement suspendu"
                );
            }
        }

        if (user instanceof Admin admin) {

            if (!admin.isActive()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Compte suspendu");
            }

            if (admin.getSuspendedUntil() != null &&
                    admin.getSuspendedUntil().isAfter(LocalDateTime.now())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Compte temporairement suspendu"
                );
            }
        }
    }
}
