package com.group2.ADN.controller;

import com.group2.ADN.dto.TicketRequest;
import com.group2.ADN.entity.Ticket;
import com.group2.ADN.entity.TicketStatus;
import com.group2.ADN.entity.User;
import com.group2.ADN.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.group2.ADN.entity.TicketType;
import com.group2.ADN.entity.TestMethod;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping
    public ResponseEntity<Ticket> createTicket(@RequestBody TicketRequest request) {
        Ticket ticketCreated = ticketService.createTicketFromRequest(request);
        return ResponseEntity.ok(ticketCreated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable Long id) {
        return ticketService.getTicketById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<Ticket> assignStaff(@PathVariable Long id) {
        Ticket ticketUpdated = ticketService.assignStaffAutomatically(id);
        return ResponseEntity.ok(ticketUpdated);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Ticket> updateStatus(@PathVariable Long id, @RequestParam TicketStatus status) {
        Ticket ticketUpdated = ticketService.updateStatus(id, status);
        return ResponseEntity.ok(ticketUpdated);
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Ticket> completeTicket(@PathVariable Long id, @RequestBody String result) {
        Ticket ticketUpdated = ticketService.completeTicket(id, result);
        return ResponseEntity.ok(ticketUpdated);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Ticket>> getTicketsByCustomer(@PathVariable Long customerId) {
        User user = ticketService.findUserById(customerId);
        return ResponseEntity.ok(ticketService.getTicketsByCustomer(user));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Ticket>> getTicketsByStatus(@PathVariable TicketStatus status) {
        return ResponseEntity.ok(ticketService.getTicketsByStatus(status));
    }

    @PostMapping("/after-payment")
    public ResponseEntity<?> createTicketAfterPayment(@RequestBody TicketRequest request, Authentication authentication) {
        // Lấy user từ authentication
        String email = authentication.getName();
        User user = ticketService.findUserByEmail(email);

        // Tính phí xét nghiệm (giả sử FE gửi đúng số tiền, hoặc bạn có thể tính lại ở đây)
        BigDecimal amount = request.getAmount();
        if (amount == null) {
            return ResponseEntity.badRequest().body("Thiếu thông tin chi phí!");
        }
        // Validate amount range
        BigDecimal min = new BigDecimal("100000");
        BigDecimal max = new BigDecimal("10000000000");


        if (amount.compareTo(min) < 0 || amount.compareTo(max) > 0) {


            return ResponseEntity.badRequest().body("❌ Số tiền không hợp lệ (100.000 ~ 10.000.000.000)");


        }
        BigDecimal currentBalance = user.getWalletBalance();
        if (currentBalance == null) currentBalance = BigDecimal.ZERO;
        if (currentBalance.compareTo(amount) < 0) {
            return ResponseEntity.badRequest().body("Số dư ví không đủ!");
        }
        // Trừ tiền
        user.setWalletBalance(currentBalance.subtract(amount));
        ticketService.saveUser(user);

        Ticket ticket = new Ticket();
        try {
            ticket.setType(TicketType.valueOf(request.getType()));
            ticket.setMethod(TestMethod.valueOf(request.getMethod()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid ticket type or method");
        }
        ticket.setReason(request.getReason());
        ticket.setStatus(TicketStatus.PENDING); // Trạng thái xử lý nghiệp vụ
        ticket.setCustomer(user);
        ticket.setAddress(request.getAddress());
        ticket.setPhone(request.getPhone());
        ticket.setEmail(request.getEmail());
        ticket.setSample1Name(request.getSample1Name());
        ticket.setSample2Name(request.getSample2Name());
        if (ticket.getMethod() == TestMethod.AT_FACILITY) {
            ticket.setAppointmentDate(request.getAppointmentDate());
        } else {
            ticket.setAppointmentDate(null);
        }
        Ticket saved = ticketService.saveTicket(ticket);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/history")
    public ResponseEntity<List<Ticket>> getHistory(Authentication authentication) {
        String email = authentication.getName();
        User user = ticketService.findUserByEmail(email);
        return ResponseEntity.ok(ticketService.getTicketsByCustomer(user));
    }

    @GetMapping("/assigned")
    public ResponseEntity<List<Ticket>> getAssigned(Authentication authentication) {
        String email = authentication.getName();
        User staff = ticketService.findUserByEmail(email);
        return ResponseEntity.ok(ticketService.getTicketsByStaff(staff));
    }

    @GetMapping("/unassigned")
    public ResponseEntity<List<Ticket>> getUnassignedTickets() {
        return ResponseEntity.ok(ticketService.getUnassignedPendingTickets());
    }

//    @PostMapping("/{id}/result")
//    public ResponseEntity<?> uploadResult(@PathVariable Long id, @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
//        return ticketService.uploadResult(id, file);
//    }
}
