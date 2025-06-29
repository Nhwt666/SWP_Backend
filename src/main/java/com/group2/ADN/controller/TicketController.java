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
import com.group2.ADN.dto.TicketFeedbackRequest;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping
    public ResponseEntity<?> createTicket(@RequestBody TicketRequest request) {
        try {
            System.out.println("🔍 DEBUG: createTicket");
            System.out.println("   Request type: " + request.getType());
            System.out.println("   Request method: " + request.getMethod());
            System.out.println("   Request status: " + request.getStatus());
            
            Ticket ticketCreated = ticketService.createTicketFromRequest(request);
            
            System.out.println("   🎯 Final ticket status: " + ticketCreated.getStatus());
            System.out.println("   🎯 Final ticket ID: " + ticketCreated.getId());
            
            return ResponseEntity.ok(Map.of(
                "message", "Ticket created successfully",
                "ticketId", ticketCreated.getId(),
                "status", ticketCreated.getStatus(),
                "type", ticketCreated.getType(),
                "method", ticketCreated.getMethod()
            ));
        } catch (Exception e) {
            System.err.println("❌ ERROR in createTicket: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Internal server error",
                "message", "An unexpected error occurred while creating the ticket"
            ));
        }
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
        try {
            // Debug logs chi tiết
            System.out.println("🔍 DEBUG: createTicketAfterPayment - START");
            System.out.println("   User: " + authentication.getName());
            System.out.println("   Authorities: " + authentication.getAuthorities());
            System.out.println("   Request type: " + request.getType());
            System.out.println("   Request method: " + request.getMethod());
            System.out.println("   Request status: " + request.getStatus());
            System.out.println("   Request amount: " + request.getAmount());
            System.out.println("   Request customerId: " + request.getCustomerId());
            System.out.println("   Request address: " + request.getAddress());
            System.out.println("   Request phone: " + request.getPhone());
            System.out.println("   Request email: " + request.getEmail());
            System.out.println("   Request sample1Name: " + request.getSample1Name());
            System.out.println("   Request sample2Name: " + request.getSample2Name());
            
            // Lấy user từ authentication
            String email = authentication.getName();
            User user = ticketService.findUserByEmail(email);
            
            System.out.println("   User role: " + user.getRole());
            System.out.println("   User ID: " + user.getId());
            System.out.println("   User wallet balance: " + user.getWalletBalance());

            // Tính phí xét nghiệm (giả sử FE gửi đúng số tiền, hoặc bạn có thể tính lại ở đây)
            BigDecimal amount = request.getAmount();
            if (amount == null) {
                System.out.println("   ❌ ERROR: Amount is null");
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Validation failed",
                    "message", "Amount is required"
                ));
            }
            
            // Validate amount range
            BigDecimal min = new BigDecimal("100000");
            BigDecimal max = new BigDecimal("10000000000");

            if (amount.compareTo(min) < 0 || amount.compareTo(max) > 0) {
                System.out.println("   ❌ ERROR: Amount out of range: " + amount);
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Validation failed",
                    "message", "Amount must be between 100,000 and 10,000,000,000"
                ));
            }
            
            BigDecimal currentBalance = user.getWalletBalance();
            if (currentBalance == null) currentBalance = BigDecimal.ZERO;
            if (currentBalance.compareTo(amount) < 0) {
                System.out.println("   ❌ ERROR: Insufficient balance. Current: " + currentBalance + ", Required: " + amount);
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Insufficient balance",
                    "message", "Wallet balance is not sufficient for this transaction"
                ));
            }
            
            // Trừ tiền
            user.setWalletBalance(currentBalance.subtract(amount));
            ticketService.saveUser(user);
            System.out.println("   ✅ Money deducted. New balance: " + user.getWalletBalance());

            Ticket ticket = new Ticket();
            try {
                ticket.setType(TicketType.valueOf(request.getType()));
                ticket.setMethod(TestMethod.valueOf(request.getMethod()));
                System.out.println("   ✅ Ticket type and method set successfully");
            } catch (IllegalArgumentException e) {
                System.out.println("   ❌ ERROR: Invalid ticket type or method: " + e.getMessage());
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Validation failed",
                    "message", "Invalid ticket type or method"
                ));
            }
            
            ticket.setReason(request.getReason());
            
            // Logic mới: CIVIL SELF_TEST → CONFIRMED, các loại khác → PENDING
            if (request.getType().equals("CIVIL") && request.getMethod().equals("SELF_TEST")) {
                ticket.setStatus(TicketStatus.CONFIRMED);
                System.out.println("   ✅ CIVIL SELF_TEST detected, setting status: CONFIRMED");
            } else {
                ticket.setStatus(TicketStatus.PENDING);
                System.out.println("   ✅ Other ticket type, setting status: PENDING");
            }
            
            ticket.setCustomer(user);
            ticket.setAddress(request.getAddress());
            ticket.setPhone(request.getPhone());
            ticket.setEmail(request.getEmail());
            ticket.setSample1Name(request.getSample1Name());
            ticket.setSample2Name(request.getSample2Name());
            ticket.setAmount(amount);
            if (ticket.getMethod() == TestMethod.AT_FACILITY) {
                ticket.setAppointmentDate(request.getAppointmentDate());
            } else {
                ticket.setAppointmentDate(null);
            }
            
            System.out.println("   🔍 About to save ticket with status: " + ticket.getStatus());
            System.out.println("   🔍 Ticket details before save:");
            System.out.println("      - Type: " + ticket.getType());
            System.out.println("      - Method: " + ticket.getMethod());
            System.out.println("      - Status: " + ticket.getStatus());
            System.out.println("      - Customer ID: " + ticket.getCustomer().getId());
            System.out.println("      - Amount: " + ticket.getAmount());
            
            Ticket saved = ticketService.saveTicket(ticket);
            System.out.println("   ✅ Ticket saved successfully!");
            System.out.println("   🎯 Final ticket status: " + saved.getStatus());
            System.out.println("   🎯 Final ticket ID: " + saved.getId());
            System.out.println("🔍 DEBUG: createTicketAfterPayment - END");
            
            return ResponseEntity.ok(Map.of(
                "message", "Ticket created successfully",
                "ticketId", saved.getId(),
                "status", saved.getStatus(),
                "type", saved.getType(),
                "method", saved.getMethod(),
                "amount", saved.getAmount()
            ));
            
        } catch (Exception e) {
            System.err.println("❌ ERROR in createTicketAfterPayment: " + e.getMessage());
            System.err.println("❌ Stack trace:");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Internal server error",
                "message", "An unexpected error occurred while creating the ticket",
                "details", e.getMessage()
            ));
        }
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

    @GetMapping("/debug/enums")
    public ResponseEntity<?> getEnums() {
        return ResponseEntity.ok(Map.of(
            "statuses", Arrays.stream(TicketStatus.values()).map(Enum::name).collect(Collectors.toList()),
            "types", Arrays.stream(TicketType.values()).map(Enum::name).collect(Collectors.toList()),
            "methods", Arrays.stream(TestMethod.values()).map(Enum::name).collect(Collectors.toList())
        ));
    }

//    @PostMapping("/{id}/result")
//    public ResponseEntity<?> uploadResult(@PathVariable Long id, @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
//        return ticketService.uploadResult(id, file);
//    }
}
