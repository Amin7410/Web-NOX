package com.nox.platform.api.dto;

import lombok.Data;

@Data
public class UserRegistrationRequest {
    private String email;
    private String password;
    private String fullName;
}
