package com.group2.ADN.repository;

import com.group2.ADN.entity.PasswordResetRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequest, Long> {

    List<PasswordResetRequest> findAllByEmail(String email);

    // Tìm OTP chưa xác thực (confirm-reset)
    @Query("SELECT r FROM PasswordResetRequest r WHERE r.otp = :otp AND r.verified = false AND r.expiresAt >= :now ORDER BY r.expiresAt DESC")
    List<PasswordResetRequest> findUnverifiedByOtp(
            @Param("otp") String otp,
            @Param("now") LocalDateTime now
    );

    // Tìm OTP đã xác thực (update-password)
    @Query("SELECT r FROM PasswordResetRequest r WHERE r.verified = true AND r.expiresAt >= :now ORDER BY r.expiresAt DESC")
    List<PasswordResetRequest> findVerifiedRequests(
            @Param("now") LocalDateTime now
    );

    @Query("SELECT r FROM PasswordResetRequest r WHERE r.otp = :otp AND r.verified = false AND r.expiresAt >= :now ORDER BY r.expiresAt DESC")
    List<PasswordResetRequest> findByOtpAndVerifiedFalseAndExpiresAtGreaterThanEqualOrderByExpiresAtDesc(
            @Param("otp") String otp,
            @Param("now") LocalDateTime now
    );


    @Query("SELECT r FROM PasswordResetRequest r WHERE r.verified = true AND r.expiresAt >= :now ORDER BY r.expiresAt DESC")
    List<PasswordResetRequest> findByVerifiedTrueAndExpiresAtGreaterThanEqualOrderByExpiresAtDesc(
            @Param("now") LocalDateTime now
    );

    @Query("SELECT r FROM PasswordResetRequest r WHERE r.otp = :otp AND r.verified = true AND r.expiresAt >= :now ORDER BY r.expiresAt DESC")
    List<PasswordResetRequest> findByOtpAndVerifiedTrueAndExpiresAtGreaterThanEqualOrderByExpiresAtDesc(
            @Param("otp") String otp,
            @Param("now") LocalDateTime now
    );


    @Transactional
    @Modifying
    @Query("DELETE FROM PasswordResetRequest p WHERE p.expiresAt < :now")
    int deleteByExpiresAtBefore(LocalDateTime now);

    @Query("SELECT p FROM PasswordResetRequest p WHERE p.expiresAt < :now")
    List<PasswordResetRequest> findExpiredRequests(LocalDateTime now);
}
