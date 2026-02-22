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

        // Extract OrgId and User context
        UUID orgId = null;
        for (Object arg : args) {
            if (arg instanceof UUID) {
                orgId = (UUID) arg;
                break;
            } else if (arg instanceof Organization) {
                orgId = ((Organization) arg).getId();
                break;
            }
        }

        if (orgId == null)
            return;

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
