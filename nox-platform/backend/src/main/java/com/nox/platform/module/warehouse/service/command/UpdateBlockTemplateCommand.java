package com.nox.platform.module.warehouse.service.command;

import java.util.Map;

public record UpdateBlockTemplateCommand(
    String name,
    String description,
    String thumbnailUrl,
    Map<String, Object> structureData,
    String version
) {}
