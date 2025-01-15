package com.msauth.msauthkeycloak.controller;

import com.msauth.msauthkeycloak.model.SignupRequest;
import com.msauth.msauthkeycloak.service.KeycloakService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
