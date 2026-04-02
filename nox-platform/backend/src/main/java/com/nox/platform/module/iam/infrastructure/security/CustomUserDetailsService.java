package com.nox.platform.module.iam.infrastructure.security;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserSecurity;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.iam.infrastructure.UserSecurityRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserSecurityRepository userSecurityRepository;

    public CustomUserDetailsService(UserRepository userRepository, UserSecurityRepository userSecurityRepository) {
        this.userRepository = userRepository;
        this.userSecurityRepository = userSecurityRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "user_details", key = "#email", unless = "#result == null")
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("[LOAD_USER] Loading user details for email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("[LOAD_USER] User not found: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        UserSecurity userSecurity = userSecurityRepository.findById(user.getId())
                .orElseThrow(() -> {
                    log.error("[LOAD_USER] Security credentials not found for user: {}", email);
                    return new UsernameNotFoundException("Security credentials not found for user: " + email);
                });

        log.debug("[LOAD_USER] Successfully loaded user: {} (ID: {})", user.getEmail(), user.getId());

        String password = userSecurity.getPasswordHash() != null ? userSecurity.getPasswordHash() : "";

        return new CustomUserDetails(
                user.getId(),
                null, // OrgId resolved later by TenantContextFilter
                user.getEmail(),
                password,
                // Roles are dynamically loaded by TenantContextFilter per request
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // Default Global Role
        );
    }
}
