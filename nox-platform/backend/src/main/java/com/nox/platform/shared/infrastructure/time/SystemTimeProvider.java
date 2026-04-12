package com.nox.platform.shared.infrastructure.time;

import com.nox.platform.shared.abstraction.TimeProvider;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;


@Component
public class SystemTimeProvider implements TimeProvider {

    @Override
    public OffsetDateTime now() {
        return OffsetDateTime.now();
    }

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public Instant currentInstant() {
        return Instant.now();
    }
}
