package com.group2.ADN.controller;

import com.group2.ADN.dto.CreateStaffRequest;
import com.group2.ADN.entity.User;
import com.group2.ADN.entity.UserRole;
import com.group2.ADN.repository.TicketRepository;
import com.group2.ADN.repository.UserRepository;
import com.group2.ADN.service.AdminService;
import com.group2.ADN.service.TicketService;
import com.group2.ADN.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.group2.ADN.entity.Ticket;
import com.group2.ADN.dto.AdminUpdateTicketRequest;
import com.group2.ADN.dto.AdminCreateTicketRequest;
import com.group2.ADN.dto.AdminUpdateUserRequest;
import com.group2.ADN.dto.AdminCreateUserRequest;
import com.group2.ADN.dto.UserWithTicketStatsDto;
import org.springframework.security.core.Authentication;

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
    private final AdminService adminService;
    private final TicketService ticketService;
    private final UserService userService;

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
        BigDecimal totalTicketSpending = ticketRepository.sumTotalAmount();

        Map<String, Object> data = Map.of(
                "totalUsers", totalUsers,
                "totalTickets", totalTickets,
                "feedbackCount", feedbackCount,
                "totalTicketSpending", totalTicketSpending != null ? totalTicketSpending : BigDecimal.ZERO
        );

        return ResponseEntity.ok(data);
    }

    @GetMapping("/deposits/stats")
    public ResponseEntity<Map<String, Object>> getDepositStats() {
        return ResponseEntity.ok(adminService.getDepositStatistics());
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

    @GetMapping("/all-users")
    public ResponseEntity<List<UserWithTicketStatsDto>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword) {
        List<UserWithTicketStatsDto> usersWithStats = userService.findUsersWithFiltersAndStats(role, keyword);
        return ResponseEntity.ok(usersWithStats);
    }

    // Ticket Management
    @GetMapping("/tickets")
    public ResponseEntity<List<Ticket>> getAllTickets() {
        List<Ticket> tickets = ticketRepository.findAll();
        return ResponseEntity.ok(tickets);
    }

    @PostMapping("/tickets")
    public ResponseEntity<Ticket> createTicket(@RequestBody AdminCreateTicketRequest request) {
        Ticket newTicket = ticketService.createTicketByAdmin(request);
        return ResponseEntity.status(201).body(newTicket);
    }

    @PutMapping("/tickets/{id}")
    public ResponseEntity<Ticket> updateTicket(@PathVariable Long id, @RequestBody AdminUpdateTicketRequest request) {
        Ticket updatedTicket = ticketService.adminUpdateTicket(id, request);
        return ResponseEntity.ok(updatedTicket);
    }

    @PutMapping("/tickets/{ticketId}/assign/{staffId}")
    public ResponseEntity<Ticket> assignTicket(@PathVariable Long ticketId, @PathVariable Long staffId) {
        Ticket assignedTicket = ticketService.assignTicketToStaff(ticketId, staffId);
        return ResponseEntity.ok(assignedTicket);
    }

    @PutMapping("/tickets/{ticketId}/unassign")
    public ResponseEntity<Ticket> unassignTicket(@PathVariable Long ticketId) {
        Ticket unassignedTicket = ticketService.unassignTicket(ticketId);
        return ResponseEntity.ok(unassignedTicket);
    }

    @DeleteMapping("/tickets/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        if (!ticketRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ticketRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody AdminUpdateUserRequest request) {
        User updatedUser = userService.updateUserByAdmin(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        // Prevent admin from deleting themselves
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentAdminEmail = authentication.getName();
        User adminUser = userRepository.findByEmail(currentAdminEmail).orElse(null);
        if (adminUser != null && adminUser.getId() == id) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUserByAdmin(@RequestBody AdminCreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã tồn tại!");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .walletBalance(BigDecimal.ZERO)
                .build();

        userRepository.save(user);
        return ResponseEntity.status(201).body(user);
    }
}
