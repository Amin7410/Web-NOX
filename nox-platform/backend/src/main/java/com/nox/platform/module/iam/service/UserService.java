package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.shared.abstraction.TimeProvider;
import com.nox.platform.shared.event.UserAccountDeletingEvent;
import com.nox.platform.shared.event.UserDeletedEvent;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TimeProvider timeProvider;

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found"));
    }

    @Transactional
    public User updateProfile(String email, String fullName, String avatarUrl) {
        User user = getUserByEmail(email);
        user.updateProfile(fullName, avatarUrl);
        user.updateTimestamp(timeProvider.now());
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found"));

        // Publish synchronous event to allow other modules (e.g. Tenant) 
        // to validate and cleanup related data.
        eventPublisher.publishEvent(new UserAccountDeletingEvent(userId));

        // Soft delete user
        user.markAsDeleted(timeProvider.now());
        userRepository.save(user);

        // Publish event to cleanup asynchronous related entities (e.g. warehouses)
        eventPublisher.publishEvent(new UserDeletedEvent(userId));
    }
}

