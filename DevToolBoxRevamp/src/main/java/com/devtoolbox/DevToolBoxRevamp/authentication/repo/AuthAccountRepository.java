package com.devtoolbox.DevToolBoxRevamp.authentication.repo;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.AuthAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface AuthAccountRepository extends JpaRepository<AuthAccount, String> {
    Optional<AuthAccount> findByProviderAndProviderId(String provider, String providerId);
}
