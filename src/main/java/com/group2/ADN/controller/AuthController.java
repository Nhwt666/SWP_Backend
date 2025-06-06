package com.group2.ADN.controller;

import com.group2.ADN.dto.LoginRequest;
import com.group2.ADN.dto.RegisterRequest;
import com.group2.ADN.entity.User;
import com.group2.ADN.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ResponseEntity.ok("User registered: " + user.getEmail());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return authService.login(request.getEmail(), request.getPassword())
                .map(user -> ResponseEntity.ok("Login successful for " + user.getEmail()))
                .orElse(ResponseEntity.status(401).body("Invalid credentials"));
    }

}