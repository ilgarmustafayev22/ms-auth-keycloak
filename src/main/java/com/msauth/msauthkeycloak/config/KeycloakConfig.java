package com.msauth.msauthkeycloak.config;

import com.msauth.msauthkeycloak.config.properties.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class KeycloakConfig {

    private final ApplicationProperties applicationProperties;

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(applicationProperties.getServerUrl())
                .realm(applicationProperties.getRealm())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(applicationProperties.getClientId())
                .clientSecret(applicationProperties.getClientSecret())
                .username(applicationProperties.getUserName())
                .password(applicationProperties.getPassword())
                .build();
    }

}
