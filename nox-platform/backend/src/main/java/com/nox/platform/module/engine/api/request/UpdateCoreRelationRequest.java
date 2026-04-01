package com.nox.platform.module.engine.api.request;

import java.util.Map;

public record UpdateCoreRelationRequest(
        Map<String, Object> rules,
        Map<String, Object> visual) {
}
