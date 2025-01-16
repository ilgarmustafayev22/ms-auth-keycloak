package com.msauth.msauthkeycloak.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.msauth.msauthkeycloak.model.LoginRequest;
import com.msauth.msauthkeycloak.model.SignupRequest;
import com.msauth.msauthkeycloak.service.KeycloakService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class KeycloakController {

    private final KeycloakService keycloakService;

    @PostMapping(value = "/signup", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> signup(@RequestBody SignupRequest request) {
        System.err.println(request.getUsername());
        return ResponseEntity.ok(keycloakService.addUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(keycloakService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Object> refreshToken(@RequestHeader("Authorization") String refresh) throws UnirestException, JsonProcessingException {
        JsonNode response = keycloakService.refreshToken(refresh);
        ObjectMapper objectMapper = new ObjectMapper();
        // Convert org.json.JSONObject to Jackson's JsonNode
        com.fasterxml.jackson.databind.JsonNode jacksonJsonNode = objectMapper.readTree(response.toString());
        return ResponseEntity.ok(jacksonJsonNode);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {

        request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        AccessToken token = ((KeycloakPrincipal<?>) request.getUserPrincipal()).getKeycloakSecurityContext().getToken();

        String userId = token.getSubject();

        keycloakService.logoutUser(userId);

        return new ResponseEntity<>("Hi!, you have logged out successfully!", HttpStatus.OK);

    }

}
