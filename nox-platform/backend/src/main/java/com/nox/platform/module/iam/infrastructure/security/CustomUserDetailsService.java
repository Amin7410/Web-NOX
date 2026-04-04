package com.nox.platform.module.iam.infrastructure.security;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserSecurity;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.iam.infrastructure.UserSecurityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;

import java.util.Collections;

@Slf4j
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
        log.info("🔍 [DEBUG_USER_DETAILS] Đang nạp UserDetails cho: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("❌ [DEBUG_USER_DETAILS] Không tìm thấy User với email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        UserSecurity userSecurity = userSecurityRepository.findById(user.getId())
                .orElseThrow(() -> {
                    log.error("❌ [DEBUG_USER_DETAILS] Không tìm thấy bản ghi bảo mật cho ID: {}", user.getId());
                    return new UsernameNotFoundException("Security credentials not found for user: " + email);
                });

        String password = userSecurity.getPasswordHash();
        if (password == null || password.isEmpty()) {
             log.error("❌ [DEBUG_USER_DETAILS] Mật khẩu Hash trong DB đang bị TRỐNG cho user: {}", email);
             password = "";
        } else {
             log.info("✅ [DEBUG_USER_DETAILS] Đã tìm thấy mật khẩu Hash cho user: {}", email);
        }

        return new CustomUserDetails(
                user.getId(),
                null, 
                user.getEmail(),
                password,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}