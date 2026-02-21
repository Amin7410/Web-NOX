package com.nox.platform.module.iam.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Email is required") 
    @Email(message = "Email should be valid") 
    String email,

    @NotBlank(message = "Password is required") 
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password,

    @Size(max = 100, message = "Full name is too long")
    String fullName
) {
    @Override
    public String toString() {
        return "RegisterRequest[email=" + email + ", fullName=" + fullName + ", password=***]";
    }
}
