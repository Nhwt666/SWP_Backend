package com.group2.ADN.service;

import com.group2.ADN.repository.PasswordResetRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordResetRequestCleanupService {

    private final PasswordResetRequestRepository passwordResetRequestRepository;

    @Scheduled(fixedRate = 120000) // 2 phút
    public void cleanExpiredPasswordResetRequests() {
        var expiredList = passwordResetRequestRepository.findExpiredRequests(LocalDateTime.now());

        if (!expiredList.isEmpty()) {
            expiredList.forEach(req ->
                    System.out.println("⚠ OTP hết hạn: " + req.getOtp() + ", email: " + req.getEmail())
            );
        }

        int deleted = passwordResetRequestRepository.deleteByExpiresAtBefore(LocalDateTime.now());

        if (deleted > 0) {
            System.out.println("🧹 Đã xóa " + deleted + " bản ghi password_reset_requests hết hạn.");
        } else {
            System.out.println("✅ Không có OTP nào hết hạn cần xóa.");
        }
    }
}
