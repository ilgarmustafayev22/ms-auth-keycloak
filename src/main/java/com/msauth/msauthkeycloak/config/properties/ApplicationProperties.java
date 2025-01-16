package com.msauth.msauthkeycloak.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "application.keycloak")
public class ApplicationProperties {

    private String serverUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
    private String userName;
    private String password;
    private String tokenUrl;

}
