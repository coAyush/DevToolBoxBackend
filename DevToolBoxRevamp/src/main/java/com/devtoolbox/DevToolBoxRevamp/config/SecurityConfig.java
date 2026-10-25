package com.devtoolbox.DevToolBoxRevamp.config;

import com.devtoolbox.DevToolBoxRevamp.security.JwtFilter;
import com.devtoolbox.DevToolBoxRevamp.security.OAuth2LoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizationSuccessHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private JwtFilter jwtFilter;
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/prices",
            "/api/login/**",
            "/api/oauth/login/**",
            "/api/signup/**",
            "/api/resendOtp",
            "/api/verifyOtp",
            "/api/tools/**",
            "/api/oauth/login",
            "/api/contact-us",
            "/api/submit/enquiry",
            "/swagger-ui/index.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/refresh/access-token",
            "/swagger-resources/**",
            "/webjars/**"
    };
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, OAuth2LoginSuccessHandler successHandler) throws Exception {
        return http
                .formLogin(form->form.disable())
                .cors(Customizer.withDefaults())
                .csrf(csrf->csrf.disable())
                .authorizeHttpRequests(authorize->authorize.requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth->oauth.successHandler(successHandler))
                .httpBasic(httpBasic->httpBasic.disable())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception->exception.authenticationEntryPoint((request,response,authStatus)->{
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().println("unauthorized or access token expired");
                }))
                .build();
    }
}
