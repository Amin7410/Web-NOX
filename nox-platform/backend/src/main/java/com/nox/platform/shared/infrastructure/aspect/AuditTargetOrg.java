package com.nox.platform.shared.infrastructure.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to explicitly mark which parameter should be used as the
 * Organization ID
 * for Audit Logging purposes. This prevents type-guessing errors in AOP.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditTargetOrg {
}
