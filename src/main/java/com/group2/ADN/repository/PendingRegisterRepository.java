package com.group2.ADN.repository;

import com.group2.ADN.entity.PendingRegister;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingRegisterRepository extends JpaRepository<PendingRegister, Long> {
    Optional<PendingRegister> findByEmailAndOtpAndVerifiedFalse(String email, String otp);
    Optional<PendingRegister> findByEmail(String email);
}
