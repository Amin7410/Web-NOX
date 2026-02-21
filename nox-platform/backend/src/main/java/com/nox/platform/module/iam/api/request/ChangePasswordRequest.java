package com.nox.platform.module.iam.api.request;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "Old Password is required") String oldPassword,
        @NotBlank(message = "New Password is required") String newPassword) {
    @Override
    public String toString() {
        return "ChangePasswordRequest[oldPassword=***, newPassword=***]";
    }
}