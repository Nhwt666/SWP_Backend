package com.group2.ADN.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.group2.ADN.dto.LoginRequest;
import com.group2.ADN.dto.PendingRegisterRequest;
import com.group2.ADN.dto.RegisterRequest;
import com.group2.ADN.entity.User;
import com.group2.ADN.repository.UserRepository;
import com.group2.ADN.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    @Autowired
    private UserRepository userRepository;

   // @PostMapping("/register")
  //  public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
  //      User user = authService.register(request);
  //      return ResponseEntity.ok("User registered: " + user.getEmail());
  //  }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        return authService.login(request.getEmail(), request.getPassword())
                .map(user -> {
                    String token = authService.generateToken(user);

                    Map<String, Object> response = new HashMap<>();
                    response.put("token", token);
                    response.put("email", user.getEmail());
                    response.put("role", user.getRole().name());

                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "Invalid credentials");
                    return ResponseEntity.status(401).body(error);
                });
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))
                .findFirst()
                .orElse("UNKNOWN");

        Map<String, Object> response = new HashMap<>();
        response.put("email", email);
        response.put("role", role);
        response.put("fullName", user.getFullName());
        response.put("phone", user.getPhone());  // ✅ Thêm số điện thoại
        response.put("walletBalance", user.getWalletBalance());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/request-register")
    public ResponseEntity<?> requestRegister(@Valid @RequestBody PendingRegisterRequest request) {
        authService.requestRegister(request);
        return ResponseEntity.ok(Map.of("message", "Đã gửi OTP xác thực email"));
    }

    @PostMapping("/confirm-register")
    public ResponseEntity<?> confirmRegister(@RequestParam String email, @RequestParam String otp) {
        authService.confirmRegister(email, otp);
        return ResponseEntity.ok(Map.of("message", "Đăng ký thành công!"));
    }

    @PostMapping("/request-reset")
    public ResponseEntity<?> requestReset(@RequestParam String email) {
        authService.requestPasswordReset(email);
        return ResponseEntity.ok(Map.of("message", "OTP đã được gửi đến email"));
    }

    @PostMapping("/confirm-reset")
    public ResponseEntity<?> confirmReset(@RequestParam String email, @RequestParam String otp) {
        authService.confirmResetPassword(email, otp);
        return ResponseEntity.ok(Map.of("message", "Xác nhận OTP thành công"));
    }

    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestParam String email, @RequestParam String newPassword) {
        authService.updatePassword(email, newPassword);
        return ResponseEntity.ok(Map.of("message", "Mật khẩu đã được cập nhật"));
    }
}