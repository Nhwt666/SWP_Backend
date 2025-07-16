package com.group2.ADN.service;

import com.group2.ADN.dto.MomoPaymentRequest;
import com.group2.ADN.dto.MomoPaymentResponse;
import com.group2.ADN.entity.TopUpHistory;
import com.group2.ADN.repository.TopUpHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.group2.ADN.repository.UserRepository;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class MomoService {
    @Value("${momo.partnerCode}")
    private String partnerCode;

    @Value("${momo.accessKey}")
    private String accessKey;

    @Value("${momo.secretKey}")
    private String secretKey;

    @Value("${momo.endpoint}")
    private String endpoint;

    @Autowired
    private TopUpHistoryRepository topUpHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    public MomoPaymentResponse createPayment(String amount, String orderInfo, String redirectUrl, String ipnUrl) {
        String requestId = UUID.randomUUID().toString();
        String orderId = UUID.randomUUID().toString();
        String requestType = "captureWallet";
        String extraData = "";

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
            history.setPaymentId(orderId);
            history.setPaymentMethod("MOMO");
            history.setStatus("PENDING");
            // Generate payerId từ userId
            history.setPayerId(generatePayerId(finalUserId));
            topUpHistoryRepository.save(history);
        }

        // Tạo raw signature string
        String rawSignature = "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;

        String signature = hmacSHA256(rawSignature, secretKey);

        MomoPaymentRequest request = new MomoPaymentRequest();
        request.setPartnerCode(partnerCode);
        request.setAccessKey(accessKey);
        request.setRequestId(requestId);
        request.setAmount(amount);
        request.setOrderId(orderId);
        request.setOrderInfo(orderInfo);
        request.setRedirectUrl(redirectUrl);
        request.setIpnUrl(ipnUrl);
        request.setRequestType(requestType);
        request.setExtraData(extraData);
        request.setSignature(signature);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MomoPaymentRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<MomoPaymentResponse> response = restTemplate.postForEntity(
                endpoint,
                entity,
                MomoPaymentResponse.class
        );
        return response.getBody();
    }

    private String hmacSHA256(String data, String key) {
        try {
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            hmacSHA256.init(secretKeySpec);
            byte[] hash = hmacSHA256.doFinal(data.getBytes());
            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error while signing data with HmacSHA256", e);
        }
    }

    // Hàm generate payerId từ userId (Base64, không padding, có prefix)
    private String generatePayerId(Long userId) {
        String raw = "PAYER" + userId;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes());
    }
} 