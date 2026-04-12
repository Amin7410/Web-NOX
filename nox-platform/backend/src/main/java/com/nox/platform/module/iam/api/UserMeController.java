package com.nox.platform.module.iam.api;

import com.nox.platform.module.iam.api.request.UpdateProfileRequest;
import com.nox.platform.module.iam.api.response.UserProfileResponse;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.service.UserService;
import com.nox.platform.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auth/me")
@RequiredArgsConstructor
public class UserMeController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(Principal principal) {
        User user = userService.getUserByEmail(principal.getName());

        return ResponseEntity.ok(ApiResponse.ok(
                new UserProfileResponse(
                        user.getId().toString(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getAvatarUrl(),
                        user.isEmailVerified())));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request, Principal principal) {
        User user = userService.updateProfile(principal.getName(), request.fullName(), request.avatarUrl());

        return ResponseEntity.ok(ApiResponse.ok(
                new UserProfileResponse(
                        user.getId().toString(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getAvatarUrl(),
                        user.isEmailVerified())));
    }
}
