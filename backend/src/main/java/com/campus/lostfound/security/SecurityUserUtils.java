package com.campus.lostfound.security;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUserUtils {

    private final JwtTokenProvider jwtTokenProvider;

    public SecurityUserUtils(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        Long fromPrincipal = parseUserId(authentication.getPrincipal());
        if (fromPrincipal != null) {
            return fromPrincipal;
        }

        Long fromCredentials = parseTokenToUserId(authentication.getCredentials());
        if (fromCredentials != null) {
            return fromCredentials;
        }

        return parseTokenToUserId(authentication.getDetails());
    }

    private Long parseTokenToUserId(Object tokenObject) {
        if (tokenObject == null) {
            return null;
        }

        String token = tokenObject.toString();
        if (token.isBlank()) {
            return null;
        }

        try {
            Claims claims = jwtTokenProvider.parseClaims(token);
            Object uid = claims.get("uid");
            if (uid != null) {
                return parseUserId(uid);
            }
            return parseUserId(claims.getSubject());
        } catch (Exception ignored) {
            return null;
        }
    }

    private Long parseUserId(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
