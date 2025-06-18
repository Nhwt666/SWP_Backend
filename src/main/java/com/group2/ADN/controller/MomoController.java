package com.group2.ADN.controller;

import com.group2.ADN.dto.MomoPaymentResponse;
import com.group2.ADN.entity.TopUpHistory;
import com.group2.ADN.repository.TopUpHistoryRepository;
import com.group2.ADN.service.MomoService;
import com.group2.ADN.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/momo")
public class MomoController {
    @Autowired
    private MomoService momoService;

    @Value("${momo.redirectUrl}")
    private String redirectUrl;

    @Value("${momo.ipnUrl}")
    private String ipnUrl;

    @Autowired
    private TopUpHistoryRepository topUpHistoryRepository;

    @Autowired
    private UserService userService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/pay")
    public ResponseEntity<MomoPaymentResponse> pay(@RequestParam double amount) {
        String orderInfo = "Nạp tiền vào ví qua MoMo";
        MomoPaymentResponse response = momoService.createPayment(String.valueOf((long)amount), orderInfo, redirectUrl, ipnUrl);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/success")
    public ResponseEntity<String> momoIpn(@RequestBody Map<String, Object> payload) {
        Integer resultCode = (payload.get("resultCode") instanceof Integer)
            ? (Integer) payload.get("resultCode")
            : Integer.parseInt(payload.get("resultCode").toString());
        String orderId = payload.get("orderId").toString();
        TopUpHistory history = topUpHistoryRepository.findByPaymentId(orderId).orElse(null);
        if (history == null) {
            return ResponseEntity.badRequest().body("Order not found");
        }
        if (resultCode == 0) {
            // Thành công
            history.setStatus("SUCCESS");
            // Lưu transId vào payerId nếu có
            if (payload.get("transId") != null) {
                history.setPayerId(payload.get("transId").toString());
            }
            topUpHistoryRepository.save(history);
            Long userId = history.getUserId();
            BigDecimal amount = history.getAmount();
            userService.topUpWallet(userId, amount, "MOMO");
            return ResponseEntity.ok("Wallet topped up successfully!");
        } else {
            // Thất bại
            history.setStatus("FAILED");
            topUpHistoryRepository.save(history);
            return ResponseEntity.ok("MoMo payment failed or not completed");
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getMomoStatus(@RequestParam String orderId) {
        TopUpHistory history = topUpHistoryRepository.findAll().stream()
            .filter(h -> orderId.equals(h.getPaymentId()))
            .findFirst().orElse(null);
        if (history == null) {
            return ResponseEntity.badRequest().body(Map.of("status", "NOT_FOUND", "message", "Order not found"));
        }
        return ResponseEntity.ok(Map.of(
            "status", history.getStatus(),
            "message", history.getStatus().equals("SUCCESS") ? "Thanh toán thành công!" : (history.getStatus().equals("FAILED") ? "Thanh toán thất bại!" : "Đang chờ thanh toán...")
        ));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmMomoPayment(@RequestParam String orderId) {
        TopUpHistory history = topUpHistoryRepository.findByPaymentId(orderId).orElse(null);
        if (history == null) {
            return ResponseEntity.badRequest().body("Order not found");
        }
        if (!"PENDING".equals(history.getStatus())) {
            return ResponseEntity.badRequest().body("Order is not in PENDING state");
        }
        // Chuyển sang SUCCESS và cộng tiền
        history.setStatus("SUCCESS");
        topUpHistoryRepository.save(history);
        userService.topUpWallet(history.getUserId(), history.getAmount(), "MOMO");
        return ResponseEntity.ok("Xác nhận thanh toán thành công!");
    }

    // Endpoint để redirect về FE với method=momo (gọi sau khi thanh toán MoMo thành công nếu muốn FE hiển thị đúng giao diện)
    @GetMapping("/redirect-success")
    public void redirectSuccess(@RequestParam String orderId, HttpServletResponse response) throws java.io.IOException {
        response.sendRedirect("http://localhost:4325/payment-success?method=momo&orderId=" + orderId);
    }
} 