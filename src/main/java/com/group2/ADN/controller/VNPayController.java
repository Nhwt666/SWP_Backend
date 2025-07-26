package com.group2.ADN.controller;

import com.group2.ADN.dto.VNPayPaymentResponse;
import com.group2.ADN.entity.TopUpHistory;
import com.group2.ADN.repository.TopUpHistoryRepository;
import com.group2.ADN.service.VNPayService;
import com.group2.ADN.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.group2.ADN.repository.UserRepository;

import java.math.BigDecimal;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.io.IOException;

@RestController
@RequestMapping("/api/vnpay")
public class VNPayController {
    @Autowired
    private VNPayService vnPayService;

    @Value("${vnpay.returnUrl}")
    private String returnUrl;

    @Value("${vnpay.ipnUrl}")
    private String ipnUrl;

    @Autowired
    private TopUpHistoryRepository topUpHistoryRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/pay")
    public ResponseEntity<VNPayPaymentResponse> pay(@RequestParam double amount) {
        try {
            // Lấy thông tin người dùng đang đăng nhập
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            Long userId = userRepository.findByEmail(email).map(u -> u.getId()).orElse(null);
            
            if (userId == null) {
                return ResponseEntity.badRequest().body(null);
            }
            
            // Thêm userId vào returnUrl để sử dụng khi redirect về
            String orderInfo = "Nap tien vao vi ADN";
            String customReturnUrl = returnUrl + "?userId=" + userId;
            
            VNPayPaymentResponse response = vnPayService.createPayment(
                    String.valueOf((int)amount), 
                    orderInfo, 
                    customReturnUrl, 
                    ipnUrl
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/ipn")
    public ResponseEntity<String> vnpayIpn(@RequestBody Map<String, Object> payload) {
        try {
            System.out.println("IPN callback received from VNPay: " + payload);
            
            String vnp_ResponseCode = payload.get("vnp_ResponseCode").toString();
            String vnp_TxnRef = payload.get("vnp_TxnRef").toString();
            String vnp_Amount = payload.get("vnp_Amount").toString();
            
            System.out.println("IPN data - TxnRef: " + vnp_TxnRef + ", ResponseCode: " + vnp_ResponseCode);
            
            TopUpHistory history = topUpHistoryRepository.findByPaymentId(vnp_TxnRef).orElse(null);
            if (history == null) {
                System.out.println("Order not found in IPN: " + vnp_TxnRef);
                return ResponseEntity.badRequest().body("Order not found");
            }

            // Kiểm tra trạng thái hiện tại để tránh xử lý trùng lặp
            if ("00".equals(vnp_ResponseCode)) {
                if ("PENDING".equals(history.getStatus())) {
                    System.out.println("Processing successful payment from IPN for: " + vnp_TxnRef);
                    // Thành công
                    history.setStatus("SUCCESS");
                    history.setPayerId(vnp_TxnRef);
                    topUpHistoryRepository.save(history);
                    
                    // Convert amount from VNPay format (x100) to actual amount
                    BigDecimal amount = new BigDecimal(vnp_Amount).divide(new BigDecimal("100"));
                    userService.topUpWallet(history.getUserId(), amount, "VNPAY");
                    System.out.println("IPN payment processed successfully: " + amount + " for user " + history.getUserId());
                } else {
                    System.out.println("IPN payment already processed, current status: " + history.getStatus());
                }
                return ResponseEntity.ok("Thanh toán thành công!");
            } else {
                // Thất bại
                System.out.println("Failed payment from IPN with code: " + vnp_ResponseCode);
                history.setStatus("FAILED");
                topUpHistoryRepository.save(history);
                return ResponseEntity.ok("Thanh toán thất bại");
            }
        } catch (Exception e) {
            System.out.println("Error processing VNPay IPN: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error processing IPN: " + e.getMessage());
        }
    }

    @GetMapping("/return")
    public void vnpayReturn(@RequestParam Map<String, String> queryParams, HttpServletResponse response) {
        try {
            String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");
            String vnp_TxnRef = queryParams.get("vnp_TxnRef");
            String vnp_Amount = queryParams.get("vnp_Amount");
            String vnp_OrderInfo = queryParams.get("vnp_OrderInfo");
            
            // Lấy userId từ query params (được thêm vào từ phương thức pay)
            String userIdStr = queryParams.get("userId");
            Long userId = userIdStr != null ? Long.parseLong(userIdStr) : null;
            
            System.out.println("Return URL received - TxnRef: " + vnp_TxnRef + ", ResponseCode: " + vnp_ResponseCode + ", UserId: " + userId);

            TopUpHistory history = topUpHistoryRepository.findByPaymentId(vnp_TxnRef).orElse(null);
            if (history == null) {
                System.out.println("Order not found: " + vnp_TxnRef);
                response.sendRedirect("http://localhost:4321/payment-failed");
                return;
            }

            if ("00".equals(vnp_ResponseCode)) {
                // Kiểm tra trạng thái hiện tại để tránh xử lý trùng lặp
                if ("PENDING".equals(history.getStatus())) {
                    System.out.println("Processing successful payment for: " + vnp_TxnRef);
                    history.setStatus("SUCCESS");
                    history.setPayerId(vnp_TxnRef);
                    topUpHistoryRepository.save(history);
                    
                    BigDecimal amount = new BigDecimal(vnp_Amount).divide(new BigDecimal("100"));
                    
                    // Nếu userId không có trong URL, lấy từ history
                    if (userId == null) {
                        userId = history.getUserId();
                    }
                    
                    userService.topUpWallet(userId, amount, "VNPAY");
                    System.out.println("Payment processed successfully: " + amount + " for user " + userId);
                } else {
                    System.out.println("Payment already processed, current status: " + history.getStatus());
                }
                
                // Chuyển hướng đến trang thành công của frontend
                response.sendRedirect("http://localhost:4321/payment-success?method=vnpay");
            } else {
                System.out.println("Failed payment with code: " + vnp_ResponseCode);
                history.setStatus("FAILED");
                topUpHistoryRepository.save(history);
                
                // Chuyển hướng đến trang thất bại của frontend
                response.sendRedirect("http://localhost:4321/payment-failed");
            }
        } catch (Exception e) {
            System.out.println("Error processing VNPay return: " + e.getMessage());
            e.printStackTrace();
            try {
                response.sendRedirect("http://localhost:4321/payment-failed");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * API để kiểm tra trạng thái thanh toán
     */
    @GetMapping("/check-status/{paymentId}")
    public ResponseEntity<?> checkPaymentStatus(@PathVariable String paymentId) {
        try {
            TopUpHistory history = topUpHistoryRepository.findByPaymentId(paymentId).orElse(null);
            if (history == null) {
                return ResponseEntity.badRequest().body("Không tìm thấy giao dịch với mã: " + paymentId);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("paymentId", history.getPaymentId());
            response.put("status", history.getStatus());
            response.put("amount", history.getAmount());
            response.put("createdAt", history.getCreatedAt());
            response.put("paymentMethod", history.getPaymentMethod());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error checking payment status: " + e.getMessage());
            return ResponseEntity.badRequest().body("Lỗi khi kiểm tra trạng thái: " + e.getMessage());
        }
    }
    
    /**
     * API để cập nhật trạng thái thanh toán thủ công (dùng cho testing)
     */
    @PostMapping("/manual-update/{paymentId}")
    public ResponseEntity<?> manualUpdateStatus(
            @PathVariable String paymentId,
            @RequestParam String status) {
        try {
            TopUpHistory history = topUpHistoryRepository.findByPaymentId(paymentId).orElse(null);
            if (history == null) {
                return ResponseEntity.badRequest().body("Không tìm thấy giao dịch với mã: " + paymentId);
            }
            
            if ("SUCCESS".equals(status) && "PENDING".equals(history.getStatus())) {
                history.setStatus("SUCCESS");
                topUpHistoryRepository.save(history);
                
                BigDecimal amount = history.getAmount();
                userService.topUpWallet(history.getUserId(), amount, "VNPAY");
                
                return ResponseEntity.ok("Đã cập nhật trạng thái thành công và nạp tiền vào ví");
            } else {
                history.setStatus(status);
                topUpHistoryRepository.save(history);
                return ResponseEntity.ok("Đã cập nhật trạng thái thành: " + status);
            }
        } catch (Exception e) {
            System.out.println("Error manually updating payment: " + e.getMessage());
            return ResponseEntity.badRequest().body("Lỗi khi cập nhật trạng thái: " + e.getMessage());
        }
    }
} 