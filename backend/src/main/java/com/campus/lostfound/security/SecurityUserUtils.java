package com.campus.lostfound.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecurityUserUtils {

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("DEBUG SecurityUserUtils: auth={}, principal={}, authorities={}",
            authentication, authentication != null ? authentication.getPrincipal() : null,
            authentication != null ? authentication.getAuthorities() : null);
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        String principalText = principal.toString();
        try {
            return Long.parseLong(principalText);
        } catch (NumberFormatException ignored) {
            log.warn("DEBUG SecurityUserUtils: principal '{}' is not a valid userId", principalText);
            return null;
        }
    }
}
