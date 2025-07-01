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
import org.springframework.http.HttpStatus;
import com.group2.ADN.dto.AdminRejectTicketRequest;
import com.group2.ADN.service.ReviewService;
import com.group2.ADN.dto.ReviewDTO;
import com.group2.ADN.entity.TopUpHistory;
import com.group2.ADN.repository.TopUpHistoryRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

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
    private final ReviewService reviewService;
    private final TopUpHistoryRepository topUpHistoryRepository;

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

    @PutMapping("/tickets/{id}/reject")
    public ResponseEntity<?> adminRejectTicket(@PathVariable Long id, @RequestBody AdminRejectTicketRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElse(null);
        if (ticket == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ticket not found");
        }
        try {
            Ticket updated = adminService.adminRejectTicket(ticket, request.getRejectedReason(), null);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    // API trả về số lượng ticket được tạo trong 5 ngày gần nhất (cho dashboard biểu đồ cột)
    @GetMapping("/ticket-stats/last-5-days")
    public ResponseEntity<List<Map<String, Object>>> getTicketStatsLast5Days() {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate fromDay = today.minusDays(4); // 5 ngày gần nhất bao gồm hôm nay
        java.time.LocalDateTime from = fromDay.atStartOfDay();
        java.time.LocalDateTime to = today.atTime(23, 59, 59);

        List<Object[]> rawStats = ticketRepository.countTicketsByCreatedAtBetweenGroupByDate(from, to);
        Map<String, Long> dateToCount = new java.util.HashMap<>();
        for (Object[] row : rawStats) {
            String date = row[0].toString();
            Long count = ((Number) row[1]).longValue();
            dateToCount.put(date, count);
        }
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (int i = 0; i < 5; i++) {
            java.time.LocalDate d = fromDay.plusDays(i);
            String dateStr = d.toString();
            long count = dateToCount.getOrDefault(dateStr, 0L);
            result.add(Map.of("date", dateStr, "count", count));
        }
        return ResponseEntity.ok(result);
    }

    // API trả về số lượng ticket theo status (pie chart), có filter theo ngày (from, to, optional)
    @GetMapping("/ticket-stats/by-status")
    public ResponseEntity<List<Map<String, Object>>> getTicketStatsByStatus(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        java.time.LocalDateTime fromDate = (from != null && !from.isEmpty()) ? java.time.LocalDate.parse(from).atStartOfDay() : null;
        java.time.LocalDateTime toDate = (to != null && !to.isEmpty()) ? java.time.LocalDate.parse(to).atTime(23, 59, 59) : null;
        List<Object[]> rawStats = ticketRepository.countTicketsByStatusWithFilter(fromDate, toDate);
        // Map status -> count
        Map<String, Long> statusToCount = new java.util.HashMap<>();
        for (Object[] row : rawStats) {
            String status = row[0].toString();
            Long count = ((Number) row[1]).longValue();
            statusToCount.put(status, count);
        }
        // Đảm bảo đủ 4 trạng thái
        String[] allStatus = {"PENDING", "IN_PROGRESS", "COMPLETED", "REJECTED"};
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (String status : allStatus) {
            long count = statusToCount.getOrDefault(status, 0L);
            result.add(Map.of("status", status, "count", count));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/recent-completed-tickets")
    public ResponseEntity<List<Map<String, Object>>> getRecentCompletedTickets() {
        return ResponseEntity.ok(adminService.getRecentCompletedTickets());
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<Map<String, Object>>> getAllReviews() {
        // Get feedback from both tickets table and reviews table
        List<Map<String, Object>> allFeedback = new java.util.ArrayList<>();
        
        // Get feedback from tickets table
        List<Ticket> ticketsWithFeedback = ticketRepository.findByRatingIsNotNullOrFeedbackIsNotNull();
        for (Ticket ticket : ticketsWithFeedback) {
            Map<String, Object> feedback = new java.util.HashMap<>();
            feedback.put("id", ticket.getId());
            feedback.put("ticketId", "TK" + String.format("%03d", ticket.getId()));
            feedback.put("customerName", ticket.getCustomer() != null ? ticket.getCustomer().getFullName() : "Unknown");
            feedback.put("rating", ticket.getRating());
            feedback.put("feedback", ticket.getFeedback());
            feedback.put("feedbackDate", ticket.getFeedbackDate() != null ? ticket.getFeedbackDate().toString() : null);
            feedback.put("status", ticket.getStatus().toString());
            feedback.put("type", ticket.getType().toString());
            feedback.put("source", "tickets");
            allFeedback.add(feedback);
        }
        
        // Get feedback from reviews table
        List<ReviewDTO> reviews = reviewService.findAllReviews();
        for (ReviewDTO review : reviews) {
            Map<String, Object> feedback = new java.util.HashMap<>();
            feedback.put("id", review.getId());
            feedback.put("ticketId", review.getTicketId());
            feedback.put("customerName", review.getCustomerName());
            feedback.put("rating", review.getRating());
            feedback.put("feedback", review.getFeedback());
            feedback.put("feedbackDate", review.getCreatedAt());
            feedback.put("source", "reviews");
            allFeedback.add(feedback);
        }
        
        // Sort by feedback date (newest first)
        allFeedback.sort((a, b) -> {
            String dateA = (String) a.get("feedbackDate");
            String dateB = (String) b.get("feedbackDate");
            if (dateA == null) return 1;
            if (dateB == null) return -1;
            return dateB.compareTo(dateA);
        });
        
        return ResponseEntity.ok(allFeedback);
    }

    @GetMapping("/tickets-with-feedback")
    public ResponseEntity<List<Map<String, Object>>> getTicketsWithFeedback() {
        List<Ticket> ticketsWithFeedback = ticketRepository.findByRatingIsNotNullOrFeedbackIsNotNull();
        
        List<Map<String, Object>> result = ticketsWithFeedback.stream()
            .map(ticket -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", ticket.getId());
                map.put("ticketId", "TK" + String.format("%03d", ticket.getId()));
                map.put("customerName", ticket.getCustomer() != null ? ticket.getCustomer().getFullName() : "Unknown");
                map.put("rating", ticket.getRating());
                map.put("feedback", ticket.getFeedback());
                map.put("feedbackDate", ticket.getFeedbackDate() != null ? ticket.getFeedbackDate().toString() : null);
                map.put("status", ticket.getStatus().toString());
                map.put("type", ticket.getType().toString());
                return map;
            })
            .collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(result);
    }

    // DTO cho lịch sử nạp tiền
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class TopUpHistoryAdminDTO {
        private Long id;
        private String userName;
        private String userEmail;
        private String paymentMethod;
        private String payerId;
        private java.math.BigDecimal amount;
        private java.time.LocalDateTime createdAt;
        private String status;
    }

    // API: Lấy toàn bộ lịch sử nạp tiền (PayPal + Momo) cho admin, trả về thông tin user và phương thức đẹp
    @GetMapping("/topup-history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TopUpHistoryAdminDTO>> getAllTopUpHistory() {
        List<TopUpHistory> historyList = topUpHistoryRepository.findAll();
        List<TopUpHistoryAdminDTO> result = historyList.stream().map(h -> {
            User user = userRepository.findById(h.getUserId()).orElse(null);
            String userName = user != null ? user.getFullName() : "Ẩn";
            String userEmail = user != null ? user.getEmail() : "Ẩn";
            return new TopUpHistoryAdminDTO(
                h.getId(),
                userName,
                userEmail,
                h.getPaymentMethod(),
                h.getPayerId(),
                h.getAmount(),
                h.getCreatedAt(),
                h.getStatus()
            );
        }).toList();
        return ResponseEntity.ok(result);
    }
}
