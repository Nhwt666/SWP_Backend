package com.group2.ADN.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    @GetMapping("/Test_Phan_Quyen")
    public ResponseEntity<String> helloCustomer() {
        return ResponseEntity.ok("Hello, Customer!");
    }
}
