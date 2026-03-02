package com.nox.platform.shared.infrastructure.aspect;

import com.nox.platform.shared.service.AuditService;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.tenant.domain.Organization;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;
    private final UserRepository userRepository;

    @AfterReturning(pointcut = "execution(* com.nox.platform.module.tenant.service.*Service.create*(..)) || " +
            "execution(* com.nox.platform.module.tenant.service.*Service.update*(..)) || " +
            "execution(* com.nox.platform.module.tenant.service.*Service.delete*(..)) || " +
            "execution(* com.nox.platform.module.tenant.service.*Service.addMember(..)) || " +
            "execution(* com.nox.platform.module.tenant.service.*Service.removeMember(..))", returning = "result")
    public void auditAction(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        UUID orgId = null;

        if (joinPoint.getSignature() instanceof org.aspectj.lang.reflect.MethodSignature methodSignature) {
            Method method = methodSignature.getMethod();
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();

            // 1. Scan for @AuditTargetOrg annotation explicitly
            for (int i = 0; i < parameterAnnotations.length; i++) {
                for (Annotation ann : parameterAnnotations[i]) {
                    if (ann instanceof AuditTargetOrg) {
                        if (args[i] instanceof UUID id) {
                            orgId = id;
                        }
                        break;
                    }
                }
                if (orgId != null)
                    break;
            }

            // 2. Fallback: Check if any argument is literally an Organization entity
            if (orgId == null) {
                for (Object arg : args) {
                    if (arg instanceof Organization org) {
                        orgId = org.getId();
                        break;
                    }
                }
            }
        }

        if (orgId == null)
            return;

        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication == null)
            return;

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails))
            return;

        String email = ((UserDetails) principal).getUsername();
        User actor = userRepository.findByEmail(email).orElse(null);
        if (actor == null)
            return;

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("method", methodName);
        metadata.put("args", args);

        auditService.record(
                orgId,
                actor.getId(),
                methodName,
                result != null ? result.getClass().getSimpleName() : "VOID",
                null, // Could extract ID from result if it's a domain entity
                metadata,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));
    }
}
