package com.group2.ADN.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/staff")
public class StaffController {

    // ✅ API kiểm tra phân quyền staff
    @GetMapping("/Test_Phan_Quyen")
    public ResponseEntity<String> helloStaff() {
        return ResponseEntity.ok("Hello, Staff!");
    }

    // ✅ (Tùy chọn) API mẫu để trả về thông tin dashboard giả lập
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
}
