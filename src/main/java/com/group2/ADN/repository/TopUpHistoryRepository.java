package com.group2.ADN.repository;

import com.group2.ADN.entity.TopUpHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopUpHistoryRepository extends JpaRepository<TopUpHistory, Long> {
    List<TopUpHistory> findByUserId(Long userId);
}