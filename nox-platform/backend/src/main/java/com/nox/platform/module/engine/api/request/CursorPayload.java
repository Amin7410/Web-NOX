package com.nox.platform.module.engine.api.request;

import lombok.Data;
import java.util.UUID;

@Data
public class CursorPayload {
    private UUID userId;
    private String userName;
    private double x;
    private double y;
}
