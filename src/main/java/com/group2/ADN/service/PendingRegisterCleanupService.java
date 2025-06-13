package com.group2.ADN.service;

import com.group2.ADN.repository.PendingRegisterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PendingRegisterCleanupService {

    private final PendingRegisterRepository pendingRegisterRepository;

    @Scheduled(fixedRate = 300000) // 5 phut
    public void cleanExpiredPendingRegisters() {
        int deleted = pendingRegisterRepository.deleteExpiredUnverified(LocalDateTime.now());
        if (deleted > 0) {
            System.out.println("🧹 Đã xóa " + deleted + " bản ghi pending_registers hết hạn.");
        }
    }
}
