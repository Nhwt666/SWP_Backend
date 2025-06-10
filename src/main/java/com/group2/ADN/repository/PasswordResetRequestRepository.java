package com.group2.ADN.repository;

import com.group2.ADN.entity.PasswordResetRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequest, Long> {

    List<PasswordResetRequest> findAllByEmailAndOtpAndVerifiedFalse(String email, String otp);
    List<PasswordResetRequest> findByEmailAndVerifiedTrueOrderByExpiresAtDesc(String email);

    List<PasswordResetRequest> findAllByEmail(String email);
}
