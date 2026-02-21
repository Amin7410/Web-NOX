package com.nox.platform.shared.util;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtils {

    private IpUtils() {
        // Utility class needs no constructor
    }

    public static String getClientIp(HttpServletRequest request) {
        /*
         * Note: X-Forwarded-For can be spoofed by the client. It is recommended to use
         * server.forward-headers-strategy=framework in application.yml and ensure the
         * application
         * runs behind a trusted reverse proxy (e.g., Nginx, AWS ALB).
         */
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
