package com.devtoolbox.DevToolBoxRevamp.security;

import com.devtoolbox.DevToolBoxRevamp.authentication.event.OAuthUserAuthenticatedEvent;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.Roles;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final ApplicationEventPublisher eventPublisher;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final RestClient.Builder restClientBuilder;
    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Roles role= (Roles) request.getSession(false).getAttribute("roles");
        if (role == null ||
                (role != Roles.DEVELOPER && role != Roles.USER)) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "You are not allowed to access this resource"
            );
            return;
        }
        OAuth2AuthenticationToken token=(OAuth2AuthenticationToken) authentication;
        String provider=token.getAuthorizedClientRegistrationId();
        OAuth2User oauth2User =token.getPrincipal();
        log.info("principle:{}",oauth2User.getAttributes());
        String name = oauth2User.getAttribute("name");
        if(name==null || name.isBlank()){
            name=oauth2User.getAttribute("login");
        }
        String email = oauth2User.getAttribute("email");
        if(email==null || email.isBlank()){
            email=fetchGithubEmail(token);
        }
        String picture;
        String providerId;
        if ("google".equals(provider)) {
            providerId = oauth2User.getAttribute("sub");
            picture = oauth2User.getAttribute("picture");
        } else if ("github".equals(provider)) {
            Object id = oauth2User.getAttribute("id");
            providerId = id!=null?id.toString():null;
            picture = oauth2User.getAttribute("avatar_url");
        } else {
            throw new IllegalArgumentException("Unsupported OAuth provider");
        }

        log.info("Google ID: {}", providerId);
        log.info("Name: {}", name);
        log.info("Email: {}", email);
        log.info("Picture: {}", picture);
        OAuthUserAuthenticatedEvent event = new OAuthUserAuthenticatedEvent(
                this,
                provider,
                providerId,
                name,
                email,
                picture,
                role
        );
        eventPublisher.publishEvent(event);
        Map<String, String> tokens = event.getTokens();
        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");
        boolean isHttps=frontendUrl.toLowerCase().startsWith("https");
        String sameSite = isHttps ? "None" : "Lax";
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .sameSite(sameSite)
                .maxAge(15*60)
                .secure(isHttps)
                .path("/api")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(false) // Use true with HTTPS
             .path("/api")
                .sameSite(sameSite)
              .maxAge(7 * 24 * 60 * 60)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        if(request.getSession()!=null){
            request.getSession(false).invalidate();
        }
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_OK);
        response.sendRedirect(frontendUrl+"/"+role.name().toLowerCase());
    }

    private String fetchGithubEmail(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client =
                authorizedClientService.loadAuthorizedClient(
                        authentication.getAuthorizedClientRegistrationId(),
                        authentication.getName()
                );

        if (client == null || client.getAccessToken() == null) {
            throw new IllegalStateException(
                    "GitHub OAuth access token not found"
            );
        }

        List<Map<String, Object>> emails = restClientBuilder.build()
                .get()
                .uri("https://api.github.com/user/emails")
                .headers(headers -> headers.setBearerAuth(
                        client.getAccessToken().getTokenValue()
                ))
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<>() {});

        if (emails == null) {
            throw new IllegalStateException(
                    "Could not retrieve GitHub email"
            );
        }

        return emails.stream()
                .filter(e -> Boolean.TRUE.equals(e.get("primary")))
                .filter(e -> Boolean.TRUE.equals(e.get("verified")))
                .map(e -> (String) e.get("email"))
                .filter(e -> e != null && !e.isBlank())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No verified primary GitHub email found"
                ));
    }
}
