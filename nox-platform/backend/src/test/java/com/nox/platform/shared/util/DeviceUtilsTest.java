package com.nox.platform.shared.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeviceUtilsTest {

    @Test
    void extractDeviceType_withNullAgent_returnsUnknown() {
        assertEquals("Unknown", DeviceUtils.extractDeviceType(null));
    }

    @Test
    void extractDeviceType_withEmptyAgent_returnsUnknown() {
        assertEquals("Unknown", DeviceUtils.extractDeviceType(""));
    }

    @Test
    void extractDeviceType_withMobileAgent_returnsMobileClass() {
        String iphoneUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1";
        String deviceClass = DeviceUtils.extractDeviceType(iphoneUserAgent);
        assertEquals("Phone", deviceClass);
    }

    @Test
    void extractDeviceType_withDesktopAgent_returnsDesktopClass() {
        String windowsUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36";
        String deviceClass = DeviceUtils.extractDeviceType(windowsUserAgent);
        assertEquals("Desktop", deviceClass);
    }
}
