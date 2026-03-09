package com.TroisN.Service.service;

import com.TroisN.Service.dto.candidate.CandidateCreateRequest;
import com.TroisN.Service.dto.client.ClientCreateRequest;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakUserService {

    private final Keycloak keycloak;

    @Value("${keycloak.admin.realm}")
    private String realm;


    public String createClientUser(ClientCreateRequest dto) {

        String userId = createUser(
                dto.getEmailAddress(),
                dto.getPassword(),
                dto.getFirstName(),
                dto.getLastName(),
                "Client"
        );

        assignRoleByUserId(userId, "CLIENT");

        return userId;
    }

    public String createAdminUser(String email, String password,
                                  String firstName, String lastName) {

        String userId = createUser(
                email,
                password,
                firstName,
                lastName,
                "Admin"
        );

        assignRoleByUserId(userId, "ADMIN");

        return userId;
    }

//    public String createCandidateUser(CandidateCreateRequest dto) {
//
//        String userId = createUser(
//                dto.getEmailAddress(),
//                dto.getPassword(),
//                dto.getFirstName(),
//                dto.getLastName(),
//                "Candidate"
//        );
//
//        assignRoleByUserId(userId, "CANDIDATE");
//
//        return userId;
//    }

    private String createUser(String email,
                              String password,
                              String firstName,
                              String lastName,
                              String defaultLastName) {

        if (firstName == null || firstName.isBlank()) {
            firstName = email.substring(0, email.indexOf("@"));
        }

        if (lastName == null || lastName.isBlank()) {
            lastName = defaultLastName;
        }

        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setAttributes(new HashMap<>());

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        user.setCredentials(List.of(credential));

        Response response = keycloak.realm(realm)
                .users()
                .create(user);

        if (response.getStatus() != 201) {
            String body = response.hasEntity()
                    ? response.readEntity(String.class)
                    : "no body";

            throw new IllegalStateException(
                    "Erreur création Keycloak - status="
                            + response.getStatus()
                            + " body=" + body
            );
        }

        return response.getLocation()
                .getPath()
                .replaceAll(".*/([^/]+)$", "$1");
    }


    public void deleteUser(String userId) {
        keycloak.realm(realm)
                .users()
                .get(userId)
                .remove();
    }

    public void assignRoleToExistingUser(String email, String roleName) {

        UserRepresentation user = getUserByEmail(email);

        assignRoleByUserId(user.getId(), roleName);
    }


    private void assignRoleByUserId(String userId, String roleName) {

        RoleRepresentation role = keycloak.realm(realm)
                .roles()
                .get(roleName)
                .toRepresentation();

        keycloak.realm(realm)
                .users()
                .get(userId)
                .roles()
                .realmLevel()
                .add(List.of(role));
    }


    public void disableUserByEmail(String email) {

        UserRepresentation user = getUserByEmail(email);

        user.setEnabled(false);

        keycloak.realm(realm)
                .users()
                .get(user.getId())
                .update(user);

        // Force logout sessions
        keycloak.realm(realm)
                .users()
                .get(user.getId())
                .logout();
    }

    public void disableUserById(String userId) {

        var userResource = keycloak.realm(realm)
                .users()
                .get(userId);

        UserRepresentation user = userResource.toRepresentation();
        user.setEnabled(false);

        userResource.update(user);
        userResource.logout();
    }


    public void enableUserByEmail(String email) {

        UserRepresentation user = getUserByEmail(email);

        user.setEnabled(true);

        keycloak.realm(realm)
                .users()
                .get(user.getId())
                .update(user);
    }

    public void enableUserById(String userId) {

        var userResource = keycloak.realm(realm)
                .users()
                .get(userId);

        UserRepresentation user = userResource.toRepresentation();
        user.setEnabled(true);

        userResource.update(user);
    }


    public void logoutUser(String userId) {
        keycloak.realm(realm)
                .users()
                .get(userId)
                .logout();
    }


    public UserRepresentation getUserByEmail(String email) {

        List<UserRepresentation> users =
                keycloak.realm(realm)
                        .users()
                        .search(email, true);

        if (users.isEmpty()) {
            throw new IllegalStateException(
                    "Utilisateur Keycloak introuvable : " + email
            );
        }

        return users.get(0);
    }


    public boolean userExists(String email) {

        List<UserRepresentation> users =
                keycloak.realm(realm)
                        .users()
                        .search(email, true);

        return !users.isEmpty();
    }
}
