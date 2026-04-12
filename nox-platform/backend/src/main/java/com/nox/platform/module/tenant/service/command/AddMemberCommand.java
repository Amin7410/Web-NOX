package com.nox.platform.module.tenant.service.command;

import java.util.UUID;

public record AddMemberCommand(
    UUID orgId,
    String email,
    String roleName,
    String inviterEmail
) {}
