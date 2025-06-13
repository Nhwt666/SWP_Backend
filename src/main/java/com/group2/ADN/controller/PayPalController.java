package com.group2.ADN.controller;

import com.group2.ADN.entity.User;
import com.group2.ADN.repository.UserRepository;
import com.group2.ADN.service.PayPalService;
import com.group2.ADN.service.UserService;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/paypal")
public class PayPalController {

    @Autowired
    private PayPalService payPalService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    @PostMapping("/pay")
    public ResponseEntity<?> pay(@RequestParam double amount) {
        try {
            // Lấy email từ người dùng đang đăng nhập (JWT)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            // Tìm user theo email
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Tạo payment
            Payment payment = payPalService.createPayment(
                    amount,
                    "USD",
                    "paypal",
                    "sale",
                    "Top-up ADN Wallet",
                    "http://localhost:8080/api/paypal/cancel",
                    "http://localhost:8080/api/paypal/success?userId=" + user.getId() + "&amount=" + amount
            );

            // Lấy approval link
            for (Links link : payment.getLinks()) {
                if (link.getRel().equals("approval_url")) {
                    return ResponseEntity.ok(link.getHref());
                }
            }

        } catch (PayPalRESTException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while processing payment");
        }

        return ResponseEntity.badRequest().body("Error while processing payment");
    }


    @GetMapping("/success")
    public void success(@RequestParam String paymentId,
                        @RequestParam("PayerID") String payerId,
                        @RequestParam Long userId,
                        @RequestParam double amount,
                        HttpServletResponse response) throws IOException {
        try {
            System.out.println("✅ PayPal SUCCESS callback!");
            System.out.println("👉 PaymentID: " + paymentId);
            System.out.println("👉 PayerID: " + payerId);
            System.out.println("👉 UserID: " + userId + ", Amount: " + amount);

            payPalService.executePayment(paymentId, payerId);
            userService.topUpWallet(userId, BigDecimal.valueOf(amount));
            System.out.println("💰 Wallet updated!");

            response.sendRedirect("http://localhost:4321/payment-success");
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            response.sendRedirect("http://localhost:4321/payment-failed");
        }
    }

}
