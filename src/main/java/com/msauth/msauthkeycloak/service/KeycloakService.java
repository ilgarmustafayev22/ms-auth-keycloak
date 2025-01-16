package com.msauth.msauthkeycloak.service;

import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.msauth.msauthkeycloak.config.KeycloakConfig;
import com.msauth.msauthkeycloak.config.properties.ApplicationProperties;
import com.msauth.msauthkeycloak.model.LoginRequest;
import com.msauth.msauthkeycloak.model.SignupRequest;
import com.msauth.msauthkeycloak.util.TokenUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final Keycloak keycloak;
    private final ApplicationProperties properties;

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

        log.info("Creating user: {}", username);
        log.info("Response Status: {}", response.getStatus());

        if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            addRealmRoleToUser(username);
            if (response.hasEntity()) {
                log.error("Response Body: {}", response.readEntity(String.class));
            }
            return "User created successfully.";
        } else {
            String errorBody = response.hasEntity() ? response.readEntity(String.class) : "No error details.";
            log.error("Error creating user: {} - {}", response.getStatus(), errorBody);
            throw new RuntimeException("User creation failed: " + errorBody);
        }

    }

    private void addRealmRoleToUser(String userName) {
        RealmResource realmResource = keycloak.realm(properties.getRealm());
        List<UserRepresentation> users = realmResource.users().search(userName);
        UserResource userResource = realmResource.users().get(users.getFirst().getId());
        RoleRepresentation role = realmResource.roles().get("user").toRepresentation();
        RoleMappingResource roleMappingResource = userResource.roles();
        roleMappingResource.realmLevel().add(Collections.singletonList(role));
    }

    private UsersResource getUsersResource() {
        return keycloak
                .realm(properties.getRealm())
                .users();
    }

    private static CredentialRepresentation createPasswordCredentials(String password) {
        CredentialRepresentation passwordCredentials = new CredentialRepresentation();
        passwordCredentials.setTemporary(false);
        passwordCredentials.setType(CredentialRepresentation.PASSWORD);
        passwordCredentials.setValue(password);
        return passwordCredentials;
    }


    public AccessTokenResponse login(LoginRequest loginRequest) {
        Keycloak keycloak = newKeycloakBuilderWithPasswordCredentials(loginRequest.getUsername(), loginRequest.getPassword()).build();
        return keycloak.tokenManager().getAccessToken();
    }

    //22222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222
    public JsonNode refreshToken(String refresh) throws UnirestException {
        String refreshToken = TokenUtil.extractToken(refresh);
        System.out.println("Using refresh token: " + refreshToken);
        System.err.println(properties.getTokenUrl());
        return Unirest.post(properties.getTokenUrl())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .field("client_id", properties.getClientId())
                .field("client_secret", properties.getClientSecret())
                .field("refresh_token", refreshToken)
                .field("grant_type", "refresh_token")
                .asJson().getBody();
    }

    public KeycloakBuilder newKeycloakBuilderWithPasswordCredentials(String username, String password) {
        return KeycloakBuilder.builder() //
                .realm(properties.getRealm()) //
                .serverUrl(properties.getServerUrl())//
                .clientId(properties.getClientId()) //
                .clientSecret(properties.getClientSecret()) //
                .username(username) //
                .password(password);
    }

    public void logoutUser(String userId) {

        UsersResource userRessource = getUsersResource();

        userRessource.get(userId).logout();

    }
//    public UserRepresentation getUserById(String userId) {
//        return getUsersResource().get(userId).toRepresentation();
//    }


//    //1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111
//    public String getByRefreshToken(String refresh) {
//        String refreshToken = extractToken(refresh);
//        System.out.println("Using refresh token: " + refreshToken);
//
//        String responseToken = null;
//        try {
//
//            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
//            urlParameters.add(new BasicNameValuePair("grant_type", "refresh_token"));
//            urlParameters.add(new BasicNameValuePair("client_id", properties.getClientId()));
//            urlParameters.add(new BasicNameValuePair("refresh_token", refreshToken));
//            urlParameters.add(new BasicNameValuePair("client_secret", properties.getClientSecret()));
//
//            responseToken = sendPost(urlParameters);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        }
//
//        return responseToken;
//    }
//
//    private String sendPost(List<NameValuePair> urlParameters) throws Exception {
//
//        CloseableHttpClient client = HttpClientBuilder.create().build();
//        String url = properties.getServerUrl() + "/realms/" + properties.getRealm() + "/protocol/openid-connect/token";
//
//        HttpPost post = new HttpPost(url);
//        System.err.println(url);
//
//        post.setEntity(new UrlEncodedFormEntity(urlParameters));
//
//        CloseableHttpResponse response = client.execute(post);
//
//        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
//
//        StringBuilder result = new StringBuilder();
//        String line = "";
//        while ((line = rd.readLine()) != null) {
//            result.append(line);
//        }
//
//        return result.toString();
//
//    }

}
