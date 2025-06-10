package com.group2.ADN.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/staff")
public class StaffController {

    @GetMapping("/Test_Phan_Quyen")
    public ResponseEntity<String> helloStaff() {
        return ResponseEntity.ok("Hello, Staff!");
    }
}
