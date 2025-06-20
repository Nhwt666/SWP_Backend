package com.group2.ADN.controller;

import com.group2.ADN.dto.UpdateProfileRequest;
import com.group2.ADN.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private UserService userService;

    @GetMapping("/Test_Phan_Quyen")
    public ResponseEntity<String> helloCustomer() {
        return ResponseEntity.ok("Hello, Customer!");
    }

    @PutMapping("/update_profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest request, Authentication authentication) {
        String email = authentication.getName();
        userService.updateProfile(email, request);
        return ResponseEntity.ok("Cập nhật thông tin thành công");
    }

}
