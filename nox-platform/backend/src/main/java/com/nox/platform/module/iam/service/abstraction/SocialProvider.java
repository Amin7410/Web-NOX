package com.nox.platform.module.iam.service.abstraction;

import java.util.Map;

public interface SocialProvider {
    boolean supports(String providerName);
    Map<String, Object> verifyToken(String token);
}
