package com.group2.ADN.controller;

import com.group2.ADN.dto.CreateStaffRequest;
import com.group2.ADN.entity.User;
import com.group2.ADN.entity.UserRole;
import com.group2.ADN.repository.TicketRepository;
import com.group2.ADN.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;

    // ✅ Kiểm tra phân quyền admin
    @GetMapping("/Test_Phan_Quyen")
    public ResponseEntity<String> helloAdmin() {
        return ResponseEntity.ok("Hello, Admin!");
    }

    // ✅ Thống kê user, ticket, feedback (nếu có)
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long totalUsers = userRepository.count();
        long totalTickets = ticketRepository.count();
        long feedbackCount = 0; // Nếu có feedbackRepo thì dùng: feedbackRepository.count();

        Map<String, Object> data = Map.of(
                "totalUsers", totalUsers,
                "totalTickets", totalTickets,
                "feedbackCount", feedbackCount
        );

        return ResponseEntity.ok(data);
    }

    // ✅ Tạo tài khoản nhân viên
    @PostMapping("/users/create")
    public ResponseEntity<?> createStaff(@RequestBody CreateStaffRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("❌ Email đã tồn tại!");
        }

        User user = User.builder()
                .fullName(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.STAFF)
                .walletBalance(BigDecimal.ZERO)
                .build();

        userRepository.save(user);
        return ResponseEntity.ok("✅ Tạo tài khoản thành công!");
    }

    // ✅ Lấy danh sách nhân viên
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllStaff() {
        List<User> staffList = userRepository.findByRole(UserRole.STAFF);
        return ResponseEntity.ok(staffList);
    }
}
