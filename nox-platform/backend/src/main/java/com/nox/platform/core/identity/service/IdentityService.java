package com.nox.platform.core.identity.service;

import com.nox.platform.api.dto.UserRegistrationRequest;
import com.nox.platform.core.identity.model.User;
import com.nox.platform.core.identity.model.UserSecurity;
import com.nox.platform.infra.persistence.identity.UserRepository;
import com.nox.platform.infra.persistence.identity.UserSecurityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IdentityService {

    private final UserRepository userRepository;
    private final UserSecurityRepository userSecurityRepository;

    @Transactional
    public User registerUser(UserRegistrationRequest request) {
        // 1. Create User
        User user = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .status("ACTIVE") // Auto-activate for smoke test
                .build();

        user = userRepository.save(user);

        // 2. Create UserSecurity (Plain text password for smoke test)
        UserSecurity userSecurity = UserSecurity.builder()
                .user(user)
                .passwordHash(request.getPassword()) // TODO: Hash this!
                .passwordSet(true)
                .build();

        userSecurityRepository.save(userSecurity);

        return user;
    }
}
