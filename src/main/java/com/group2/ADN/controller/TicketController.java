package com.group2.ADN.controller;

import com.group2.ADN.dto.TicketRequest;
import com.group2.ADN.entity.Ticket;
import com.group2.ADN.entity.TicketStatus;
import com.group2.ADN.entity.User;
import com.group2.ADN.entity.TicketType;
import com.group2.ADN.entity.TestMethod;
import com.group2.ADN.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
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


    /**
     * Tạo ticket sau khi thanh toán thành công
     */
    @PostMapping("/after-payment")
    public ResponseEntity<?> createTicketAfterPayment(@RequestBody TicketRequest request, Authentication authentication) {
        try {
            // Lấy user từ authentication
            String email = authentication.getName();
            User user = ticketService.findUserByEmail(email);
            Ticket saved = ticketService.createTicketAfterPayment(request, user);
            return ResponseEntity.ok(Map.of(
                    "message", "Ticket created successfully",
                    "ticketId", saved.getId(),
                    "status", saved.getStatus(),
                    "type", saved.getType(),
                    "method", saved.getMethod(),
                    "amount", saved.getAmount()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Validation failed",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Internal server error",
                    "message", "An unexpected error occurred while creating the ticket",
                    "details", e.getMessage()
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



    /**
     * Lấy lịch sử ticket của user hiện tại
     */
    @GetMapping("/history")
    public ResponseEntity<List<Ticket>> getHistory(Authentication authentication) {
        String email = authentication.getName();
        User user = ticketService.findUserByEmail(email);
        return ResponseEntity.ok(ticketService.getTicketsByCustomer(user));
    }

    /**
     * Lấy danh sách ticket được gán cho staff hiện tại
     */
    @GetMapping("/assigned")
    public ResponseEntity<List<Ticket>> getAssigned(Authentication authentication) {
        String email = authentication.getName();
        User staff = ticketService.findUserByEmail(email);
        return ResponseEntity.ok(ticketService.getTicketsByStaff(staff));
    }

    /**
     * Lấy danh sách ticket chưa được gán staff
     */
    @GetMapping("/unassigned")
    public ResponseEntity<List<Ticket>> getUnassignedTickets() {
        return ResponseEntity.ok(ticketService.getUnassignedPendingTickets());
    }

    /**
     * Lấy danh sách các giá trị enum cho ticket (status, type, method)
     */
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

/*    @PostMapping
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
    }*/
}
