package com.msauth.msauthkeycloak.service;

import com.msauth.msauthkeycloak.model.SignupRequest;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    public Object addUser(SignupRequest request) {
        String username = request.getUsername();

        CredentialRepresentation credential = createPasswordCredentials(request.getPassword());

        UserRepresentation user = new UserRepresentation();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(username);
        user.setEmail(request.getEmail());
        user.setEmailVerified(false);
        user.setCredentials(Collections.singletonList(credential));
        user.setEnabled(true);

        UsersResource usersResource = getUsersResource();
        Response response = usersResource.create(user);

        System.out.println("Creating user: " + username);
        System.out.println("Response Status: " + response.getStatus());

        if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            if (response.hasEntity()) {
                System.out.println("Response Body: " + response.readEntity(String.class));
            }
            return "User created successfully.";
        } else {
            String errorBody = response.hasEntity() ? response.readEntity(String.class) : "No error details.";
            System.err.println("Error creating user: " + response.getStatus() + " - " + errorBody);
            throw new RuntimeException("User creation failed: " + errorBody);
        }

    }

    private void addRealmRoleToUser(String userName) {
        RealmResource realmResource = keycloak.realm(realm);
        List<UserRepresentation> users = realmResource.users().search(userName);
        UserResource userResource = realmResource.users().get(users.get(0).getId());
        RoleRepresentation role = realmResource.roles().get("user").toRepresentation();
        RoleMappingResource roleMappingResource = userResource.roles();
        roleMappingResource.realmLevel().add(Collections.singletonList(role));
    }

    private UsersResource getUsersResource() {
//        return KeycloakBuilder.builder()
//                .serverUrl(serverUrl)
//                .realm(realm)
//                .grantType(OAuth2Constants.PASSWORD)
//                .username("ilgar222")
//                .password("ilgar222")
//                .clientId(clientId)
//                .clientSecret(clientSecret)
//                .build()
//                .realm(realm).users();

        RealmResource realm1 = keycloak.realm(realm);
        return realm1.users();

    }

    private static CredentialRepresentation createPasswordCredentials(String password) {
        CredentialRepresentation passwordCredentials = new CredentialRepresentation();
        passwordCredentials.setTemporary(false);
        passwordCredentials.setType(CredentialRepresentation.PASSWORD);
        passwordCredentials.setValue(password);
        return passwordCredentials;
    }

    public UserRepresentation getUserById(String userId) {
        return  getUsersResource().get(userId).toRepresentation();
    }

}
