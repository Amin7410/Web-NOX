package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.tenant.domain.OrgMember;
import com.nox.platform.module.tenant.domain.Organization;
import com.nox.platform.module.tenant.domain.Role;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.shared.event.UserDeletedEvent;
import com.nox.platform.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrgMemberRepository orgMemberRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserService userService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder().email("test@nox.com").fullName("Test User").status(UserStatus.ACTIVE).build();
        user.setId(userId);
    }

    @Test
    void deleteUser_UserNotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void deleteUser_SoleOwner_ThrowsException() {
        Organization org = Organization.builder().name("Test Org").build();
        org.setId(UUID.randomUUID());

        Role ownerRole = Role.builder().name("OWNER").build();
        OrgMember member = OrgMember.builder().user(user).organization(org).role(ownerRole).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(orgMemberRepository.findByUserId(userId)).thenReturn(List.of(member));
        when(orgMemberRepository.countByOrganizationIdAndRoleName(org.getId(), "OWNER")).thenReturn(1L);

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("User is the sole owner of organization");

        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void deleteUser_NotSoleOwner_Success() {
        Organization org = Organization.builder().name("Test Org").build();
        org.setId(UUID.randomUUID());

        Role ownerRole = Role.builder().name("OWNER").build();
        OrgMember member = OrgMember.builder().user(user).organization(org).role(ownerRole).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(orgMemberRepository.findByUserId(userId)).thenReturn(List.of(member));
        when(orgMemberRepository.countByOrganizationIdAndRoleName(org.getId(), "OWNER")).thenReturn(2L); // 2 owners

        userService.deleteUser(userId);

        assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(user.getDeletedAt()).isNotNull();

        verify(orgMemberRepository).deleteAll(List.of(member));
        verify(userRepository).save(user);

        ArgumentCaptor<UserDeletedEvent> eventCaptor = ArgumentCaptor.forClass(UserDeletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().userId()).isEqualTo(userId);
    }

    @Test
    void deleteUser_NoMemberships_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(orgMemberRepository.findByUserId(userId)).thenReturn(List.of());

        userService.deleteUser(userId);

        assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(user.getDeletedAt()).isNotNull();

        verify(orgMemberRepository).deleteAll(List.of());
        verify(userRepository).save(user);
        verify(eventPublisher).publishEvent(any(UserDeletedEvent.class));
    }
}
