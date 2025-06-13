package com.group2.ADN.repository;

import com.group2.ADN.entity.PendingRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PendingRegisterRepository extends JpaRepository<PendingRegister, Long> {

    Optional<PendingRegister> findByEmailAndOtpAndVerifiedFalse(String email, String otp);
    Optional<PendingRegister> findByEmail(String email);

    @Modifying
    @Transactional
    @Query("DELETE FROM PendingRegister p WHERE p.verified = false AND p.expiresAt < :time")
    int deleteExpiredUnverified(@Param("time") LocalDateTime time);
}
