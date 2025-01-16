package com.msauth.msauthkeycloak.util;

import io.micrometer.common.util.StringUtils;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TokenUtil {

    public static String extractToken(final String authorizationHeader) {
        validateAuthorizationHeader(authorizationHeader);
        return authorizationHeader.replace("Bearer ", "").trim();
    }

    public static void validateAuthorizationHeader(String authorizationHeader) {
        if (StringUtils.isBlank(authorizationHeader) || !authorizationHeader.startsWith("Bearer ") ||
                StringUtils.isBlank(authorizationHeader.replace("Bearer ", "")))
            throw new IllegalArgumentException("Authorization header is missing or invalid");
    }

}
