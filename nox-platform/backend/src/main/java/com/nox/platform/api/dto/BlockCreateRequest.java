package com.nox.platform.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockCreateRequest {
    private UUID templateId;
    private double x;
    private double y;
}
