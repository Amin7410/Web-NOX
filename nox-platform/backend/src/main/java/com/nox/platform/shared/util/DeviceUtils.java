package com.nox.platform.shared.util;

import nl.basjes.parse.useragent.UserAgentAnalyzer;

public class DeviceUtils {

    private static final UserAgentAnalyzer uaa = UserAgentAnalyzer.newBuilder()
            .hideMatcherLoadStats()
            .withCache(10000)
            .build();

    /**
     * Parses the raw User-Agent header into a generalized device class (e.g.
     * "Phone", "Desktop", "Tablet").
     */
    public static String extractDeviceType(String rawUserAgent) {
        if (rawUserAgent == null || rawUserAgent.isEmpty()) {
            return "Unknown";
        }
        nl.basjes.parse.useragent.UserAgent parsedAgent = uaa.parse(rawUserAgent);
        return parsedAgent.getValue("DeviceClass");
    }
}
