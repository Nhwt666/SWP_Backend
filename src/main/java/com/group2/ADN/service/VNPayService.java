package com.group2.ADN.service;

import com.group2.ADN.dto.VNPayResponse;
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
import jakarta.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VNPayService {

    @Value("${vnpay.version}")
    private String vnpVersion;

    @Value("${vnpay.tmnCode}")
    private String vnpTmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnpHashSecret;

    @Value("${vnpay.payUrl}")
    private String vnpPayUrl;

    @Value("${vnpay.returnUrl}")
    private String vnpReturnUrl;

    @Autowired
    private TopUpHistoryRepository topUpHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    public VNPayResponse createPayment(String amount, String orderInfo, HttpServletRequest request) {
        String vnp_TxnRef = getTransactionRef();
        String vnp_Command = "pay";
        String orderType = "topup";

        // Lấy userId từ SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;
        if (authentication != null) {
            String email = authentication.getName();
            userId = userRepository.findByEmail(email).map(u -> u.getId()).orElse(null);
        }

        // Lưu lịch sử nạp tiền với trạng thái chờ xác nhận
        if (userId != null) {
            final Long finalUserId = userId;
            TopUpHistory history = new TopUpHistory();
            history.setUserId(finalUserId);
            history.setAmount(new BigDecimal(amount));
            history.setCreatedAt(LocalDateTime.now());
            history.setPaymentId(vnp_TxnRef);
            history.setPaymentMethod("VNPAY");
            history.setStatus("PENDING");
            // Generate payerId từ userId
            history.setPayerId(generatePayerId(finalUserId));
            topUpHistoryRepository.save(history);
        }

        // Amount phải là số nguyên và có đơn vị là VND * 100
        long amountInVND = Long.parseLong(amount) * 100;

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnpVersion);
        vnpParams.put("vnp_Command", vnp_Command);
        vnpParams.put("vnp_TmnCode", vnpTmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amountInVND));
        vnpParams.put("vnp_CurrCode", "VND");

        // Thông tin ngân hàng thanh toán
        vnpParams.put("vnp_BankCode", "NCB"); // Default bank code

        // Thông tin đơn hàng
        vnpParams.put("vnp_TxnRef", vnp_TxnRef);
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", orderType);

        // Ngôn ngữ
        vnpParams.put("vnp_Locale", "vn");

        // Thông tin return URL
        vnpParams.put("vnp_ReturnUrl", vnpReturnUrl);

        // Thông tin IP
        vnpParams.put("vnp_IpAddr", getIpAddress(request));

        // Thời gian tạo giao dịch
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_CreateDate", vnp_CreateDate);

        // Tạo URL thanh toán
        String paymentUrl = createPaymentUrl(vnpParams);

        VNPayResponse response = new VNPayResponse();
        response.setPaymentUrl(paymentUrl);
        response.setCode("00");
        response.setMessage("success");
        response.setVnp_TxnRef(vnp_TxnRef);

        return response;
    }

    private String getIpAddress(HttpServletRequest request) {
        String ipAddress;
        ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    private String getTransactionRef() {
        return String.valueOf(System.currentTimeMillis());
    }

    private String createPaymentUrl(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        try {
            for (String fieldName : fieldNames) {
                String fieldValue = params.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    // Build hash data
                    hashData.append(fieldName).append("=").append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    
                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
                            .append("=")
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    
                    if (fieldNames.indexOf(fieldName) < fieldNames.size() - 1) {
                        hashData.append("&");
                        query.append("&");
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error encoding URL parameters", e);
        }
        
        String vnpSecureHash = hmacSHA512(vnpHashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnpSecureHash);
        
        return vnpPayUrl + "?" + query;
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            hmac.init(secretKeySpec);
            byte[] hash = hmac.doFinal(data.getBytes());
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error creating HMAC SHA512", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    // Hàm generate payerId từ userId (Base64, không padding, có prefix)
    private String generatePayerId(Long userId) {
        String raw = "VNPAYER" + userId;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes());
    }
} 