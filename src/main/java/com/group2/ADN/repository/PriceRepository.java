package com.group2.ADN.repository;

import com.group2.ADN.entity.Price;
import com.group2.ADN.entity.PriceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceRepository extends JpaRepository<Price, Long> {
    List<Price> findByType(PriceType type);
} 