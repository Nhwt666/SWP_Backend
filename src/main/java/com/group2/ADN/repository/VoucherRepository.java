package com.group2.ADN.repository;

import com.group2.ADN.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    
    @Query("SELECT v FROM Voucher v WHERE v.status = :status AND v.start <= :now AND v.endDate >= :now")
    List<Voucher> findByStatusAndStartLessThanEqualAndEndDateGreaterThanEqual(
        @Param("status") String status, 
        @Param("now") LocalDateTime now
    );
    
    Voucher findFirstByNameIgnoreCase(String name);
} 