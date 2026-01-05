package com.TroisN.Service.service;

import com.TroisN.Service.dto.candidate.CandidateCreateRequest;
import com.TroisN.Service.dto.client.ClientCreateRequest;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;

@Service
public class KeycloakUserService {

    private final Keycloak keycloak;

    @Value("${keycloak.admin.realm}")
    private String realm;

    public KeycloakUserService(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    public String createClientUser(ClientCreateRequest dto) {

        String email = dto.getEmailAddress();

        String firstName = dto.getFirstName();
        String lastName = dto.getLastName();

        if (firstName == null || firstName.isBlank()) {
            firstName = email.substring(0, email.indexOf("@"));
        }

        if (lastName == null || lastName.isBlank()) {
            lastName = "Client";
        }

        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setAttributes(new HashMap<>());

        CredentialRepresentation password = new CredentialRepresentation();
        password.setType(CredentialRepresentation.PASSWORD);
        password.setValue(dto.getPassword());
        password.setTemporary(false);

        user.setCredentials(List.of(password));

        Response response = keycloak.realm(realm)
                .users()
                .create(user);

        if (response.getStatus() != 201) {
            String body = response.hasEntity() ? response.readEntity(String.class) : "no body";
            throw new IllegalStateException(
                    "Erreur création Keycloak - status=" + response.getStatus() + " body=" + body
            );
        }

        String userId = response.getLocation()
                .getPath()
                .replaceAll(".*/([^/]+)$", "$1");

        RoleRepresentation role = keycloak.realm(realm)
                .roles()
                .get("CLIENT")
                .toRepresentation();

        keycloak.realm(realm)
                .users()
                .get(userId)
                .roles()
                .realmLevel()
                .add(List.of(role));

        return userId;
    }

    public String createAdminUser(String email, String password, String firstName, String lastName) {

        if (firstName == null || firstName.isBlank()) {
            firstName = email.substring(0, email.indexOf("@"));
        }

        if (lastName == null || lastName.isBlank()) {
            lastName = "Admin";
        }

        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        user.setCredentials(List.of(credential));

        Response response = keycloak.realm(realm)
                .users()
                .create(user);

        if (response.getStatus() != 201) {
            String body = response.hasEntity() ? response.readEntity(String.class) : "no body";
            throw new IllegalStateException(
                    "Erreur création Keycloak - status=" + response.getStatus() + " body=" + body
            );
        }

        String userId = response.getLocation()
                .getPath()
                .replaceAll(".*/([^/]+)$", "$1");

        RoleRepresentation adminRole = keycloak.realm(realm)
                .roles()
                .get("ADMIN")
                .toRepresentation();

        keycloak.realm(realm)
                .users()
                .get(userId)
                .roles()
                .realmLevel()
                .add(List.of(adminRole));

        return userId;
    }

    public String createCandidateUser(CandidateCreateRequest dto) {

        String email = dto.getEmailAddress();
        String password = dto.getPassword();

        String firstName = dto.getFirstName();
        String lastName = dto.getLastName();

        if (firstName == null || firstName.isBlank()) {
            firstName = email.substring(0, email.indexOf("@"));
        }

        if (lastName == null || lastName.isBlank()) {
            lastName = "Candidate";
        }

        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        user.setCredentials(List.of(credential));

        Response response = keycloak.realm(realm)
                .users()
                .create(user);

        if (response.getStatus() != 201) {
            String body = response.hasEntity() ? response.readEntity(String.class) : "no body";
            throw new IllegalStateException(
                    "Erreur création Keycloak - status=" + response.getStatus() + " body=" + body
            );
        }

        String userId = response.getLocation()
                .getPath()
                .replaceAll(".*/([^/]+)$", "$1");

        RoleRepresentation role = keycloak.realm(realm)
                .roles()
                .get("CANDIDATE")
                .toRepresentation();

        keycloak.realm(realm)
                .users()
                .get(userId)
                .roles()
                .realmLevel()
                .add(List.of(role));

        return userId;
    }

    public void deleteUser(String userId) {
        keycloak.realm(realm)
                .users()
                .get(userId)
                .remove();
    }

    public void assignRoleToExistingUser(String email, String roleName) {

        var realmResource = keycloak.realm(realm);

        List<UserRepresentation> users = realmResource.users().search(email, true);

        if (users.isEmpty()) {
            throw new IllegalStateException("Utilisateur Keycloak introuvable : " + email);
        }

        UserRepresentation user = users.get(0);

        RoleRepresentation role = realmResource.roles()
                .get(roleName)
                .toRepresentation();

        realmResource.users()
                .get(user.getId())
                .roles()
                .realmLevel()
                .add(List.of(role));
    }
}
