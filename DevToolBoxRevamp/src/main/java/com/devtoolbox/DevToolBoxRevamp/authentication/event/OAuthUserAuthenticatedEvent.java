package com.devtoolbox.DevToolBoxRevamp.authentication.event;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.Roles;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.HashMap;
import java.util.Map;

@Getter
public class OAuthUserAuthenticatedEvent extends ApplicationEvent {

    private final String provider;
    private final String providerId;
    private final String name;
    private final String email;
    private final String picture;
    private final Roles role;

    private final Map<String, String> tokens = new HashMap<>();

    public OAuthUserAuthenticatedEvent(
            Object source,
            String provider,
            String providerId,
            String name,
            String email,
            String picture,
            Roles role) {

        super(source);
        this.provider = provider;
        this.providerId = providerId;
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.role = role;
    }
}