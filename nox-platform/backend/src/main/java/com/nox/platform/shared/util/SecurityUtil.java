package com.nox.platform.shared.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import com.nox.platform.shared.security.NoxUserDetails;

import java.util.UUID;

/**
 * @deprecated Use {@link com.nox.platform.shared.abstraction.SecurityProvider} instead for better testability and SOLID compliance.
 */
@Deprecated
public class SecurityUtil {

    private SecurityUtil() {
        // Utility class
    }

    /**
     * @deprecated Use SecurityProvider.getCurrentUserId()
     */
    @Deprecated
    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof NoxUserDetails noxUserDetails) {
            return noxUserDetails.getId();
        }
        return null;
    }

    /**
     * @deprecated Use SecurityProvider.getCurrentOrganizationId()
     */
    @Deprecated
    public static UUID getCurrentOrganizationId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof NoxUserDetails noxUserDetails) {
            return noxUserDetails.getOrganizationId();
        }
        return null;
    }

    /**
     * @deprecated Use SecurityProvider.getCurrentUserEmail()
     */
    @Deprecated
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return null;
    }
}
