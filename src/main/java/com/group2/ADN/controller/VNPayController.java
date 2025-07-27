package com.group2.ADN.controller;

import com.group2.ADN.dto.VNPayResponse;
import com.group2.ADN.entity.TopUpHistory;
import com.group2.ADN.repository.TopUpHistoryRepository;
import com.group2.ADN.repository.UserRepository;
import com.group2.ADN.service.UserService;
import com.group2.ADN.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/vnpay")
public class VNPayController {
    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private TopUpHistoryRepository topUpHistoryRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/pay")
    public ResponseEntity<VNPayResponse> pay(@RequestParam double amount, HttpServletRequest request) {
        String orderInfo = "Nạp tiền vào ví qua VNPay";
        VNPayResponse response = vnPayService.createPayment(String.valueOf((long)amount), orderInfo, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/success")
    public ResponseEntity<?> success(
            @RequestParam Map<String, String> queryParams,
            HttpServletResponse response) throws IOException {
        String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");
        String vnp_TxnRef = queryParams.get("vnp_TxnRef");
        String vnp_Amount = queryParams.get("vnp_Amount");
        String vnp_BankCode = queryParams.get("vnp_BankCode");

        // VNPay callback received - processing...

        // Xử lý callback logic
        ResponseEntity<?> result = processVNPayCallback(queryParams);
        
        // Redirect về frontend với kết quả
        if (result.getStatusCode().is2xxSuccessful()) {
            response.sendRedirect("http://localhost:4322/payment-success?method=vnpay&status=success");
        } else {
            response.sendRedirect("http://localhost:4322/payment-failed?method=vnpay&status=failed");
        }
        
        return result;
    }

    private ResponseEntity<?> processVNPayCallback(Map<String, String> queryParams) {
        String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");
        String vnp_TxnRef = queryParams.get("vnp_TxnRef");
        String vnp_Amount = queryParams.get("vnp_Amount");
        String vnp_BankCode = queryParams.get("vnp_BankCode");

        if ("00".equals(vnp_ResponseCode)) {
            // Payment succeeded
            TopUpHistory history = topUpHistoryRepository.findByPaymentId(vnp_TxnRef).orElse(null);
            if (history != null) {
                // Kiểm tra xem đã xử lý chưa để tránh duplicate
                if ("SUCCESS".equals(history.getStatus())) {
                    return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Giao dịch đã được xử lý trước đó",
                        "amount", history.getAmount(),
                        "paymentId", vnp_TxnRef
                    ));
                }
                
                history.setStatus("SUCCESS");
                history.setPayerId(vnp_BankCode); // Using BankCode as PayerId
                topUpHistoryRepository.save(history);

                // Top up user wallet
                BigDecimal amount = new BigDecimal(Long.parseLong(vnp_Amount) / 100); // Convert back from VND*100
                System.out.println("VNPay payment successful - Adding " + amount + " to user " + history.getUserId());
                userService.topUpWallet(history.getUserId(), amount, "VNPAY");

                // Trả về giống PayPal
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Nạp tiền thành công qua VNPay",
                    "amount", amount,
                    "paymentId", vnp_TxnRef
                ));
            } else {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "failed",
                    "message", "Không tìm thấy giao dịch"
                ));
            }
        } else {
            // Payment failed
            TopUpHistory history = topUpHistoryRepository.findByPaymentId(vnp_TxnRef).orElse(null);
            if (history != null) {
                history.setStatus("FAILED");
                topUpHistoryRepository.save(history);
            }
            return ResponseEntity.status(400).body(Map.of(
                "status", "failed",
                "message", "Thanh toán thất bại qua VNPay"
            ));
        }
    }

    // Lấy lịch sử nạp tiền qua VNPay cho user hiện tại
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/topup-history")
    public ResponseEntity<List<TopUpHistory>> getTopUpHistory(Authentication authentication) {
        String email = authentication.getName();
        var user = userRepository.findByEmail(email).orElseThrow();
        List<TopUpHistory> history = topUpHistoryRepository.findByUserId(user.getId())
            .stream().filter(h -> "VNPAY".equals(h.getPaymentMethod())).toList();
        return ResponseEntity.ok(history);
    }

    // Endpoint để FE forward VNPay callback về backend
    @PostMapping("/callback")
    public ResponseEntity<?> handleCallback(@RequestBody Map<String, String> callbackData) {
        return processVNPayCallback(callbackData);
    }
} 