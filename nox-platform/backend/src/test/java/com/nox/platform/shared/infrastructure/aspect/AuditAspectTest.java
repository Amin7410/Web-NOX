package com.nox.platform.shared.infrastructure.aspect;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.tenant.domain.Organization;
import com.nox.platform.shared.service.AuditService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditAspectTest {

    @Mock
    private AuditService auditService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuditAspect auditAspect;

    private MockHttpServletRequest mockRequest;
    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        mockRequest = new MockHttpServletRequest();
        mockRequest.setRemoteAddr("127.0.0.1");
        mockRequest.addHeader("User-Agent", "Test-Agent");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    // Dummy service to test method references natively
    static class DummyService {
        public void updateSomething(@AuditTargetOrg UUID orgId, String data) {
        }

        public void createSomethingElse(Organization organization, String data) {
        }

        public void unannotatedMethod(UUID someId, String data) {
        }
    }

    @Test
    void auditAction_withAuditTargetOrgAnnotation_Success() throws NoSuchMethodException {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Method method = DummyService.class.getMethod("updateSomething", UUID.class, String.class);

        setupMocks(method, new Object[] { orgId, "xyz" }, userId);

        auditAspect.auditAction(createJoinPoint(method, new Object[] { orgId, "xyz" }), "ResultString");

        ArgumentCaptor<UUID> orgCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<UUID> userCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> methodCaptor = ArgumentCaptor.forClass(String.class);

        verify(auditService).record(orgCaptor.capture(), userCaptor.capture(), methodCaptor.capture(), anyString(),
                any(), anyMap(), anyString(), anyString());

        assertThat(orgCaptor.getValue()).isEqualTo(orgId);
        assertThat(userCaptor.getValue()).isEqualTo(userId);
        assertThat(methodCaptor.getValue()).isEqualTo("updateSomething");
    }

    @Test
    void auditAction_withOrganizationFallback_Success() throws NoSuchMethodException {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Organization org = Organization.builder().name("Test").build();
        org.setId(orgId);

        Method method = DummyService.class.getMethod("createSomethingElse", Organization.class, String.class);

        setupMocks(method, new Object[] { org, "xyz" }, userId);

        auditAspect.auditAction(createJoinPoint(method, new Object[] { org, "xyz" }), "ResultString");

        verify(auditService).record(eq(orgId), eq(userId), eq("createSomethingElse"), anyString(), any(), anyMap(),
                anyString(), anyString());
    }

    @Test
    void auditAction_NoOrgIdExtracted_DoesNothing() throws NoSuchMethodException {
        Method method = DummyService.class.getMethod("unannotatedMethod", UUID.class, String.class);

        // Neither UUID nor Org is annotated. Should abort early.
        auditAspect.auditAction(createJoinPoint(method, new Object[] { UUID.randomUUID(), "xyz" }), "ResultString");

        verify(auditService, never()).record(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void auditAction_NoPrincipal_DoesNothing() throws NoSuchMethodException {
        UUID orgId = UUID.randomUUID();
        Method method = DummyService.class.getMethod("updateSomething", UUID.class, String.class);

        when(securityContext.getAuthentication()).thenReturn(null);

        auditAspect.auditAction(createJoinPoint(method, new Object[] { orgId, "xyz" }), "ResultString");

        verify(auditService, never()).record(any(), any(), any(), any(), any(), any(), any(), any());
    }

    private void setupMocks(Method method, Object[] args, UUID userId) {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("actor@nox.com");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        User actor = User.builder().email("actor@nox.com").build();
        actor.setId(userId);
        when(userRepository.findByEmail("actor@nox.com")).thenReturn(Optional.of(actor));
    }

    private JoinPoint createJoinPoint(Method method, Object[] args) {
        JoinPoint joinPoint = mock(JoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(args);
        when(signature.getName()).thenReturn(method.getName());
        when(signature.getMethod()).thenReturn(method);

        return joinPoint;
    }
}
