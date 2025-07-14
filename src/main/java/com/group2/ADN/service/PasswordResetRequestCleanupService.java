package com.group2.ADN.service;

import com.group2.ADN.repository.PasswordResetRequestRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordResetRequestCleanupService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetRequestCleanupService.class);
    private final PasswordResetRequestRepository passwordResetRequestRepository;

    @Scheduled(fixedRate = 120000) // 2 phút
    public void cleanExpiredPasswordResetRequests() {
        var expiredList = passwordResetRequestRepository.findExpiredRequests(LocalDateTime.now());

        if (!expiredList.isEmpty()) {
            expiredList.forEach(req ->
                log.warn("[OTP] OTP hết hạn: {}, email: {}", req.getOtp(), req.getEmail())
            );
        }

        int deleted = passwordResetRequestRepository.deleteByExpiresAtBefore(LocalDateTime.now());

        if (deleted > 0) {
            log.info("[OTP] Đã xóa {} bản ghi password_reset_requests hết hạn.", deleted);
        } else {
            log.info("[OTP] Không có OTP nào hết hạn cần xóa.");
        }
    }
}
