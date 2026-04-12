package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.service.abstraction.SocialProvider;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SocialAuthVerificationService {

    private final List<SocialProvider> socialProviders;

    public Map<String, Object> verifyToken(String provider, String token) {
        if (provider == null || token == null || token.isBlank()) {
            throw new DomainException("INVALID_SOCIAL_TOKEN", "Provider and token must not be empty");
        }

        return socialProviders.stream()
                .filter(p -> p.supports(provider))
                .findFirst()
                .map(p -> p.verifyToken(token))
                .orElseThrow(() -> new DomainException("INVALID_SOCIAL_TOKEN", 
                    "No provider support found for: " + provider));
    }
}


