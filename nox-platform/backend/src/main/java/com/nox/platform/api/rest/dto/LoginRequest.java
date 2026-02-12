package com.nox.platform.api.rest.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
