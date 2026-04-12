package com.nox.platform.shared.abstraction;

import java.time.OffsetDateTime;
import java.time.Instant;

public interface TimeProvider {
    OffsetDateTime now();
    long currentTimeMillis();
    Instant currentInstant();
}
