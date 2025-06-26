package com.group2.ADN.controller;

import com.group2.ADN.dto.UpdateProfileRequest;
import com.group2.ADN.dto.TicketFeedbackRequest;
import com.group2.ADN.entity.Ticket;
import com.group2.ADN.entity.TicketStatus;
import com.group2.ADN.entity.User;
import com.group2.ADN.entity.TicketFeedback;
import com.group2.ADN.service.UserService;
import com.group2.ADN.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private UserService userService;

    @Autowired
    private TicketService ticketService;

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

    @PutMapping("/tickets/{id}/feedback")
    public ResponseEntity<?> submitFeedback(@PathVariable Long id, @RequestBody TicketFeedbackRequest request, Authentication authentication) {
        String email = authentication.getName();
        User user = ticketService.findUserByEmail(email);
        Ticket ticket = ticketService.getTicketById(id).orElse(null);
        if (ticket == null || !ticket.getCustomer().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not allowed");
        }
        if (ticket.getStatus() != TicketStatus.COMPLETED) {
            return ResponseEntity.badRequest().body("Can only feedback on completed tickets");
        }
        if (ticketService.getFeedback(ticket, user) != null) {
            return ResponseEntity.badRequest().body("Feedback already submitted for this ticket");
        }
        if (request.getRating() != null && (request.getRating() < 1 || request.getRating() > 5)) {
            return ResponseEntity.badRequest().body("Rating must be between 1 and 5");
        }
        ticketService.submitFeedback(ticket, user, request);
        return ResponseEntity.ok("Feedback submitted");
    }

    @PutMapping("/tickets/{id}/feedback/update")
    public ResponseEntity<?> updateFeedback(@PathVariable Long id, @RequestBody TicketFeedbackRequest request, Authentication authentication) {
        String email = authentication.getName();
        User user = ticketService.findUserByEmail(email);
        Ticket ticket = ticketService.getTicketById(id).orElse(null);
        if (ticket == null || !ticket.getCustomer().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not allowed");
        }
        if (ticket.getStatus() != TicketStatus.COMPLETED) {
            return ResponseEntity.badRequest().body("Can only update feedback on completed tickets");
        }
        if (ticketService.getFeedback(ticket, user) == null) {
            return ResponseEntity.badRequest().body("No feedback to update. Please submit feedback first.");
        }
        if (request.getRating() != null && (request.getRating() < 1 || request.getRating() > 5)) {
            return ResponseEntity.badRequest().body("Rating must be between 1 and 5");
        }
        ticketService.updateFeedback(ticket, user, request);
        return ResponseEntity.ok("Feedback updated");
    }

}
