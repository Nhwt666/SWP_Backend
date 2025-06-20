package com.group2.ADN.controller;

import com.group2.ADN.dto.CreateStaffRequest;
import com.group2.ADN.entity.User;
import com.group2.ADN.entity.UserRole;
import com.group2.ADN.repository.TicketRepository;
import com.group2.ADN.repository.UserRepository;
import com.group2.ADN.service.AdminStatsService;
import com.group2.ADN.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import com.group2.ADN.dto.DoctorWithTicketCountDTO;
import com.group2.ADN.entity.Ticket;
import java.util.Collections;
import com.group2.ADN.dto.MoneyStatsDTO;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminStatsService adminStatsService;
    private final TicketService ticketService;

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

    @GetMapping("/money-stats")
    public ResponseEntity<MoneyStatsDTO> getMoneyStats(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return ResponseEntity.ok(adminStatsService.getMoneyStats(startDate, endDate));
    }

    // Lấy danh sách bác sĩ
    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorWithTicketCountDTO>> getAllDoctors() {
        List<User> doctors = userRepository.findByRole(UserRole.STAFF);
        List<DoctorWithTicketCountDTO> result = doctors.stream().map(doctor -> {
            int ticketCount = ticketRepository.countByStaffAndStatusIn(
                doctor,
                java.util.List.of(
                    com.group2.ADN.entity.TicketStatus.PENDING,
                    com.group2.ADN.entity.TicketStatus.IN_PROGRESS
                )
            );
            return new DoctorWithTicketCountDTO(
                doctor.getId(),
                doctor.getFullName(),
                doctor.getEmail(),
                doctor.getPhone(),
                ticketCount
            );
        }).toList();
        return ResponseEntity.ok(result);
    }

    // Tạo tài khoản bác sĩ
    @PostMapping("/doctors")
    public ResponseEntity<?> createDoctor(@RequestBody CreateStaffRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("❌ Email đã tồn tại!");
        }
        User user = User.builder()
                .fullName(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.STAFF)
                .walletBalance(BigDecimal.ZERO)
                .phone(request.getPhone())
                .build();
        userRepository.save(user);
        return ResponseEntity.ok("✅ Tạo tài khoản bác sĩ thành công!");
    }

    // Sửa thông tin bác sĩ
    @PutMapping("/doctors/{id}")
    public ResponseEntity<?> updateDoctor(@PathVariable Long id, @RequestBody CreateStaffRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ!"));
        user.setFullName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        userRepository.save(user);
        return ResponseEntity.ok("✅ Cập nhật thành công!");
    }

    // Xóa tài khoản bác sĩ
    @DeleteMapping("/doctors/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok("✅ Đã xóa tài khoản bác sĩ!");
    }

    // Lấy danh sách ticket của một bác sĩ
    @GetMapping("/doctors/{id}/tickets")
    public ResponseEntity<List<Ticket>> getTicketsByDoctor(@PathVariable Long id) {
        User doctor = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ!"));

        // Đảm bảo user này là STAFF
        if (doctor.getRole() != UserRole.STAFF) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        List<Ticket> tickets = ticketRepository.findByStaff(doctor);
        return ResponseEntity.ok(tickets);
    }

    // Gỡ ticket khỏi bác sĩ
    @PutMapping("/tickets/{id}/unassign")
    public ResponseEntity<Ticket> unassignTicket(@PathVariable Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ticket!"));

        ticket.setStaff(null);
        ticket.setStatus(com.group2.ADN.entity.TicketStatus.PENDING);

        Ticket updatedTicket = ticketRepository.save(ticket);
        return ResponseEntity.ok(updatedTicket);
    }

    // TẠO, SỬA, XÓA, LẤY DANH SÁCH KHÁCH HÀNG (CUSTOMERS)
    // ----------------------------------------------------

    @GetMapping("/customers")
    public ResponseEntity<List<User>> getAllCustomers() {
        return ResponseEntity.ok(userRepository.findByRole(UserRole.CUSTOMER));
    }

    @PostMapping("/customers")
    public ResponseEntity<?> createCustomer(@RequestBody CreateStaffRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã tồn tại!");
        }
        User user = User.builder()
                .fullName(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.CUSTOMER)
                .walletBalance(BigDecimal.ZERO)
                .phone(request.getPhone())
                .build();
        userRepository.save(user);
        return ResponseEntity.ok("Tạo tài khoản khách hàng thành công!");
    }

    @PutMapping("/customers/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @RequestBody CreateStaffRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với ID: " + id));

        if (user.getRole() != UserRole.CUSTOMER) {
            return ResponseEntity.badRequest().body("Người dùng này không phải là khách hàng.");
        }

        user.setFullName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);
        return ResponseEntity.ok("Cập nhật thông tin khách hàng thành công!");
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với ID: " + id));

        if (user.getRole() != UserRole.CUSTOMER) {
            return ResponseEntity.badRequest().body("Không thể xóa người dùng không phải khách hàng.");
        }

        userRepository.deleteById(id);
        return ResponseEntity.ok("Đã xóa khách hàng thành công!");
    }

    @GetMapping("/users/{userId}/tickets")
    public ResponseEntity<List<Ticket>> getTicketsForUser(@PathVariable Long userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ticketRepository.findByCustomerId(userId));
    }

    // LẤY VÀ XÓA TICKET
    // ----------------------------------------------------

    @GetMapping("/tickets")
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return ResponseEntity.ok(ticketRepository.findAll());
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable Long id) {
        return ticketService.getTicketById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/tickets")
    public ResponseEntity<Ticket> createTicket(@RequestBody com.group2.ADN.dto.TicketRequest request) {
        Ticket createdTicket = ticketService.createTicketFromRequest(request);
        return ResponseEntity.ok(createdTicket);
    }

    @PutMapping("/tickets/{id}")
    public ResponseEntity<Ticket> updateTicket(@PathVariable Long id, @RequestBody com.group2.ADN.dto.TicketRequest request) {
        Ticket updatedTicket = ticketService.updateTicketFromRequest(id, request);
        return ResponseEntity.ok(updatedTicket);
    }

    @DeleteMapping("/tickets/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable Long id) {
        if (!ticketRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ticketRepository.deleteById(id);
        return ResponseEntity.ok("Đã xóa ticket thành công!");
    }
}
