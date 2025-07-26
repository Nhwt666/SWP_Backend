package com.group2.ADN.service;

import com.group2.ADN.dto.VNPayPaymentResponse;
import com.group2.ADN.entity.TopUpHistory;
import com.group2.ADN.repository.TopUpHistoryRepository;
import com.group2.ADN.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class VNPayService {
    @Value("${vnpay.tmnCode}")
    private String tmnCode;

    @Value("${vnpay.hashSecret}")
    private String hashSecret;

    @Value("${vnpay.payUrl}")
    private String payUrl;

    @Value("${vnpay.version}")
    private String version;

    @Value("${vnpay.command}")
    private String command;

    @Value("${vnpay.currCode}")
    private String currCode;

    @Value("${vnpay.locale}")
    private String locale;

    @Autowired
    private TopUpHistoryRepository topUpHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    public VNPayPaymentResponse createPayment(String amount, String orderInfo, String returnUrl, String ipnUrl) {
        try {
            // Lưu trữ thông tin thanh toán vào database trước
            String vnp_TxnRef = generateOrderId();
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = null;
            if (authentication != null) {
                String email = authentication.getName();
                userId = userRepository.findByEmail(email).map(u -> u.getId()).orElse(null);
            }
    
            if (userId != null) {
                final Long finalUserId = userId;
                TopUpHistory history = new TopUpHistory();
                history.setUserId(finalUserId);
                history.setAmount(new BigDecimal(amount));
                history.setCreatedAt(LocalDateTime.now());
                history.setPaymentId(vnp_TxnRef);
                history.setPaymentMethod("VNPAY");
                history.setStatus("PENDING");
                history.setPayerId(generatePayerId(finalUserId));
                topUpHistoryRepository.save(history);
            }
            
            // Cách tiếp cận hoàn toàn mới, sử dụng chính xác code của VNPay
            String vnp_IpAddr = "127.0.0.1";
            String vnp_Amount = String.valueOf(Long.parseLong(amount) * 100);
            String vnp_Command = "pay";
            String vnp_CreateDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String vnp_CurrCode = "VND";
            String vnp_Locale = "vn";
            String vnp_OrderType = "other";
            String vnp_Version = "2.1.0";
            
            // Tạo Map và sắp xếp theo thứ tự alphabet
            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", tmnCode);
            vnp_Params.put("vnp_Amount", vnp_Amount);
            vnp_Params.put("vnp_CurrCode", vnp_CurrCode);
            vnp_Params.put("vnp_BankCode", "INTCARD");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", orderInfo);
            vnp_Params.put("vnp_OrderType", vnp_OrderType);
            vnp_Params.put("vnp_Locale", vnp_Locale);
            vnp_Params.put("vnp_ReturnUrl", returnUrl);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            
            // Sắp xếp tham số theo alphabet
            Map<String, String> sortedParams = new TreeMap<>(vnp_Params);
            
            // Tạo chuỗi hash
            StringBuilder hashData = new StringBuilder();
            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                hashData.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII)).append("&");
            }
            
            // Xóa dấu & cuối cùng
            if (hashData.charAt(hashData.length() - 1) == '&') {
                hashData.setLength(hashData.length() - 1);
            }
            
            // Tạo HMAC-SHA512
            String vnp_SecureHash = hmacSHA512(hashSecret, hashData.toString());
            
            // Thêm hash vào cuối tham số
            sortedParams.put("vnp_SecureHash", vnp_SecureHash);
            
            // Tạo URL
            StringBuilder paymentUrlBuilder = new StringBuilder(payUrl);
            paymentUrlBuilder.append("?");
            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                paymentUrlBuilder.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII)).append("&");
            }
            
            // Xóa dấu & cuối cùng
            if (paymentUrlBuilder.charAt(paymentUrlBuilder.length() - 1) == '&') {
                paymentUrlBuilder.setLength(paymentUrlBuilder.length() - 1);
            }
            
            String paymentUrl = paymentUrlBuilder.toString();
            
            VNPayPaymentResponse response = new VNPayPaymentResponse();
            response.setPayUrl(paymentUrl);
            response.setOrderId(vnp_TxnRef);
            response.setAmount(amount);
            response.setMessage("Tạo thanh toán VNPay thành công");
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo URL thanh toán VNPay: " + e.getMessage(), e);
        }
    }
    
    public boolean verifyHash(Map<String, Object> params) {
        if (params.get("vnp_SecureHash") == null) {
            return false;
        }
        
        String vnp_SecureHash = params.get("vnp_SecureHash").toString();
        
        // Tạo bản sao của params và loại bỏ vnp_SecureHash
        Map<String, Object> paramsCopy = new HashMap<>(params);
        paramsCopy.remove("vnp_SecureHash");
        paramsCopy.remove("vnp_SecureHashType");
        
        // Sắp xếp params theo thứ tự alphabet
        TreeMap<String, String> sortedParams = new TreeMap<>();
        for (Map.Entry<String, Object> entry : paramsCopy.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().toString().isEmpty()) {
                sortedParams.put(entry.getKey(), entry.getValue().toString());
            }
        }
        
        // Tạo chuỗi ký
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            hashData.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        
        // Loại bỏ ký tự & cuối cùng
        if (hashData.length() > 0) {
            hashData.setLength(hashData.length() - 1);
        }

        String calculatedHash = hmacSHA512(hashSecret, hashData.toString());
        return calculatedHash.equals(vnp_SecureHash);
    }

    public boolean verifyHashString(Map<String, String> params) {
        if (params.get("vnp_SecureHash") == null) {
            return false;
        }
        
        String vnp_SecureHash = params.get("vnp_SecureHash");
        
        // Tạo bản sao của params và loại bỏ vnp_SecureHash
        Map<String, String> paramsCopy = new HashMap<>(params);
        paramsCopy.remove("vnp_SecureHash");
        paramsCopy.remove("vnp_SecureHashType");
        
        // Sắp xếp params theo thứ tự alphabet
        TreeMap<String, String> sortedParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : paramsCopy.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                sortedParams.put(entry.getKey(), entry.getValue());
            }
        }
        
        // Tạo chuỗi ký
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            hashData.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        
        // Loại bỏ ký tự & cuối cùng
        if (hashData.length() > 0) {
            hashData.setLength(hashData.length() - 1);
        }

        String calculatedHash = hmacSHA512(hashSecret, hashData.toString());
        return calculatedHash.equals(vnp_SecureHash);
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA512");
            hmac.init(secretKey);
            byte[] hash = hmac.doFinal(data.getBytes("UTF-8"));
            
            // Convert to hex string - giống với hmac.digest("hex") trong NodeJS
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error while signing data with HmacSHA512", e);
        }
    }

    private String generateOrderId() {
        return "VNPAY_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    private String generatePayerId(Long userId) {
        return "VNPAY_PAYER_" + userId + "_" + System.currentTimeMillis();
    }
}
