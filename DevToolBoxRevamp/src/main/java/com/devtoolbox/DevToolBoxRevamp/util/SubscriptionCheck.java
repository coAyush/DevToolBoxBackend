package com.devtoolbox.DevToolBoxRevamp.util;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.SubscriptionDetails;
import com.devtoolbox.DevToolBoxRevamp.common.CurrentUserPrincipal;
import com.devtoolbox.DevToolBoxRevamp.users.repo.ToolUsageRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class SubscriptionCheck extends OncePerRequestFilter {
    private final ToolUsageRepository  toolUsageRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();
        return !path.startsWith("/api/users/tools");
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CurrentUserPrincipal principal)) {
            filterChain.doFilter(request, response);
            return;
        }

        SubscriptionDetails details = toolUsageRepository.getSubscriptionDetails(principal.userId);
        if (details == null || details.getUserId() == null || details.getSubscriptionId() == null
                || !"USER".equalsIgnoreCase(details.getRole())) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean isFree = "FREE".equalsIgnoreCase(details.getTier());
        boolean planExpired = !isFree
                && (details.getEndDate() == null || !details.getEndDate().isAfter(Instant.now()));
        boolean effectivelyFree = isFree || planExpired;

        long limit = effectivelyFree ? 200 : 500;
        long used = details.getUsageCount() == null ? 0 : details.getUsageCount();

        if (used >= limit) {
            log.debug("limit reached");
            String msg = effectivelyFree
                    ? "Free limit exceeded for the month. Subscribe or wait for next month to use the services."
                    : "Pro limit exceeded for the month. Please wait for next month's reset.";
            reject(response, planExpired ? "Plan expired, you are on free tier now. " + msg : msg);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":\"LIMIT_EXCEEDED\",\"message\":\"" + message + "\"}");
    }
}
