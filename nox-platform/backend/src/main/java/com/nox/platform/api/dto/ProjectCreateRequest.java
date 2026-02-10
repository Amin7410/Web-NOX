package com.nox.platform.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCreateRequest {
    private String name;
    private String slug;
    private String description;
    private UUID creatorId; // Optional, defaults to first user
}
