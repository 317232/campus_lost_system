package com.campus.lostfound.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final com.campus.lostfound.common.utils.TokenBlacklistCache tokenBlacklistCache;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, com.campus.lostfound.common.utils.TokenBlacklistCache tokenBlacklistCache) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistCache = tokenBlacklistCache;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            String token = authorization.substring(BEARER_PREFIX.length());

            if (tokenBlacklistCache.isBlacklisted(token)) {
                // Token is blacklisted, ignore it
                filterChain.doFilter(request, response);
                return;
            }

            try {
                Claims claims = jwtTokenProvider.parseClaims(token);
                String username = claims.getSubject();
                String role = claims.get("role", String.class);
                log.info("JWT filter: username={}, role={}", username, role);
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                var authentication = new UsernamePasswordAuthenticationToken(username, token, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                log.warn("JWT filter: parse failed, err={}, clearing context", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
