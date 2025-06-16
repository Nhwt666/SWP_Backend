package com.group2.ADN.controller;

import com.group2.ADN.repository.UserRepository;
import com.group2.ADN.repository.TicketRepository;
// import com.group2.ADN.repository.FeedbackRepository; // Nếu có

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    // private final FeedbackRepository feedbackRepository; // Bỏ comment nếu có

    @GetMapping("/Test_Phan_Quyen")
    public ResponseEntity<String> helloAdmin() {
        return ResponseEntity.ok("Hello, Admin!");
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long totalUsers = userRepository.count();
        long totalTickets = ticketRepository.count();
        long feedbackCount = 0; // Thay bằng feedbackRepository.count() nếu có

        Map<String, Object> data = Map.of(
                "totalUsers", totalUsers,
                "totalTickets", totalTickets,
                "feedbackCount", feedbackCount
        );

        return ResponseEntity.ok(data);
    }
}
