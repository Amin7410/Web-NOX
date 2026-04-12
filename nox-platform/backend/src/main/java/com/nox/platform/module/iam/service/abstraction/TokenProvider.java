package com.nox.platform.module.iam.service.abstraction;

import java.util.Map;
import org.springframework.security.core.userdetails.UserDetails;

public interface TokenProvider {
    String generateToken(String username);
    String generateToken(Map<String, Object> extraClaims, String username);
    /**
     * Extracts a specific claim from the token.
     */
    <T> T extractClaim(String token, java.util.function.Function<io.jsonwebtoken.Claims, T> claimsResolver);

    String extractUsername(String token);
    boolean isTokenValid(String token, UserDetails userDetails);
    /**
     * Generates a random high-entropy refresh token.
     */
    String generateRefreshToken();
}
