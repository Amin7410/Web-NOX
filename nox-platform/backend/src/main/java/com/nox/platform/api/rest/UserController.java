package com.nox.platform.api.rest;

import com.nox.platform.api.dto.UserRegistrationRequest;
import com.nox.platform.core.identity.model.User;
import com.nox.platform.core.identity.service.IdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final IdentityService identityService;

    @PostMapping
    public ResponseEntity<User> registerUser(@RequestBody UserRegistrationRequest request) {
        User createdUser = identityService.registerUser(request);
        return ResponseEntity.ok(createdUser);
    }
}
