package com.group2.ADN.controller;

import com.group2.ADN.service.PayPalService;
import com.group2.ADN.service.UserService;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/pay")
    public ResponseEntity<?> pay(@RequestParam double amount, @RequestParam Long userId) {
        try {
            Payment payment = payPalService.createPayment(amount, "USD", "paypal",
                    "sale", "Top-up ADN Wallet",
                    "http://localhost:8080/api/paypal/cancel",
                    "http://localhost:8080/api/paypal/success?userId=" + userId + "&amount=" + amount);

            for (Links link : payment.getLinks()) {
                if (link.getRel().equals("approval_url")) {
                    return ResponseEntity.ok(link.getHref());
                }
            }
        } catch (PayPalRESTException e) {
            e.printStackTrace();
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

            response.sendRedirect("http://localhost:3000/payment-success");
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            response.sendRedirect("http://localhost:3000/payment-failed");
        }
    }

}
