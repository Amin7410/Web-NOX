package com.nox.platform.module.engine.api.request;

import java.util.Map;
import java.util.UUID;

public record UpdateCoreBlockRequest(
        String name,
        UUID parentBlockId,
        Map<String, Object> config,
        Map<String, Object> visual) {
}
