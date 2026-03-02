package com.nox.platform.shared.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IpUtilsTest {

    @Test
    void getClientIp_withXForwardedFor_returnsIp() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100, 10.0.0.1");

        String result = IpUtils.getClientIp(request);

        assertEquals("192.168.1.100", result);
    }

    @Test
    void getClientIp_withXRealIpFallback_returnsIp() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("10.0.0.5");

        String result = IpUtils.getClientIp(request);

        assertEquals("10.0.0.5", result);
    }

    @Test
    void getClientIp_withRemoteAddrFallback_returnsIp() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getHeader("X-Real-IP")).thenReturn("unknown");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        String result = IpUtils.getClientIp(request);

        assertEquals("127.0.0.1", result);
    }
}
