package com.devtoolbox.DevToolBoxRevamp.security;

import com.devtoolbox.DevToolBoxRevamp.common.CurrentUserPrincipal;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.Roles;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken=resolveToken(request);
        if(accessToken==null || accessToken.isBlank()){
            filterChain.doFilter(request,response);
            return;
        }
        try {

            if (!jwtService.isTokenValid(accessToken)) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Invalid or expired token"
                );
                return;
            }

            String id = jwtService.extractId(accessToken);
            String email = jwtService.extractEmail(accessToken);
            String name = jwtService.extractName(accessToken);
            String phone = jwtService.extractPhone(accessToken);
            Roles role = jwtService.extractRole(accessToken);

            CurrentUserPrincipal principal =
                    CurrentUserPrincipal.builder()
                            .userId(UUID.fromString(id))
                            .email(email)
                            .name(name)
                            .phone(phone)
                            .role(role)
                            .build();

            var authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + role)
            );

            UsernamePasswordAuthenticationToken authenticated =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            authorities
                    );

            SecurityContextHolder.getContext()
                    .setAuthentication(authenticated);

        } catch (io.jsonwebtoken.JwtException
                 | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid token"
            );
            return;
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path =request.getServletPath();
        boolean skip= path.startsWith("/api/login/")
                || path.startsWith("/api/signup/")
                || path.startsWith("/api/oauth/login")
                || path.equals("/api/resendOtp")
                || path.equals("/api/verifyOtp")
                || path.equals("/api/refresh/access-token");
        log.debug(request.getRequestURI());
        log.debug("skipping it");
        return skip;
    }

    private String resolveToken(HttpServletRequest request) {
        if(request.getHeader("Authorization")!=null && request.getHeader("Authorization").startsWith("Bearer "))
            return request.getHeader("Authorization").substring(7);
        Cookie[] cookies = request.getCookies();
        log.debug(Arrays.toString(cookies));
        if (cookies != null) {
            for(Cookie c : cookies) {
                if ("accessToken".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}
