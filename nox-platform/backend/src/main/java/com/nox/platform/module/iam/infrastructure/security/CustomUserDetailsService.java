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

import java.util.Collections;

@Service
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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        UserSecurity userSecurity = userSecurityRepository.findById(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Security credentials not found for user: " + email));

        String password = userSecurity.getPasswordHash() != null ? userSecurity.getPasswordHash() : "";

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                password,
                // Roles are dynamically loaded by TenantContextFilter per request
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // Default Global Role
        );
    }
}
