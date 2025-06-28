package com.group2.ADN.controller;

import com.group2.ADN.dto.UpdateProfileRequest;
import com.group2.ADN.dto.TicketFeedbackRequest;
import com.group2.ADN.entity.Ticket;
import com.group2.ADN.entity.TicketStatus;
import com.group2.ADN.entity.User;
import com.group2.ADN.entity.TicketFeedback;
import com.group2.ADN.service.UserService;
import com.group2.ADN.service.TicketService;
import com.group2.ADN.service.ReviewService;
import com.group2.ADN.dto.FeedbackRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private UserService userService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private ReviewService reviewService;

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

    @PutMapping("/tickets/{ticketId}/feedback")
    public ResponseEntity<?> submitFeedback(
            @PathVariable Long ticketId,
            @Valid @RequestBody FeedbackRequest request,
            Authentication authentication) {
        try {
            log.info("Feedback request for ticket: {}, user: {}", ticketId, authentication.getName());
            
            String email = authentication.getName();
            User user = ticketService.findUserByEmail(email);
            
            Ticket updatedTicket = ticketService.submitTicketFeedback(ticketId, request, user.getId());
            
            log.info("Feedback submitted successfully for ticket: {}", ticketId);
            
            return ResponseEntity.ok(Map.of(
                "message", "Feedback submitted successfully",
                "ticketId", updatedTicket.getId(),
                "rating", updatedTicket.getRating(),
                "feedback", updatedTicket.getFeedback(),
                "feedbackDate", updatedTicket.getFeedbackDate()
            ));
        } catch (RuntimeException e) {
            log.error("Error submitting feedback for ticket {}: {}", ticketId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Bad Request",
                "message", e.getMessage()
            ));
        }
    }

}
