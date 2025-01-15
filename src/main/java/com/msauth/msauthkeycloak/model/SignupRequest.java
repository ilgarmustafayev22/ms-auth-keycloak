package com.msauth.msauthkeycloak.model;

import lombok.Data;

@Data
public class SignupRequest {

    private String email;
    private String firstName;
    private String lastName;
    private String username;
    private String password;

}
