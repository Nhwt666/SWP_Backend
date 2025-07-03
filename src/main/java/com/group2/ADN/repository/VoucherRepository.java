package com.group2.ADN.repository;

import com.group2.ADN.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    List<Voucher> findByStatus(String status);
    List<Voucher> findByStatusAndStartLessThanEqualAndEndDateGreaterThanEqual(String status, LocalDateTime now1, LocalDateTime now2);
    Voucher findFirstByNameIgnoreCase(String name);
} 