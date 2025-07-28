package com.group2.ADN.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.group2.ADN.dto.AssignResultRequest;
import com.group2.ADN.entity.Ticket;
import com.group2.ADN.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/staff")
public class StaffController {

    @Autowired
    private TicketService ticketService;

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/Test_Phan_Quyen")
    public ResponseEntity<String> helloStaff() {
        return ResponseEntity.ok("Hello, Staff!");
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/dashboard-info")
    public ResponseEntity<?> getDashboardInfo() {
        return ResponseEntity.ok(
                java.util.Map.of(
                        "pendingTickets", 12,
                        "completedTasks", 34,
                        "todayTasks", 5
                )
        );
    }

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{id}/complete")
    public ResponseEntity<Ticket> completeTicket(@PathVariable Long id, @RequestBody String result) {
        Ticket ticketUpdated = ticketService.completeTicket(id, result);
        return ResponseEntity.ok(ticketUpdated);
    }

}
