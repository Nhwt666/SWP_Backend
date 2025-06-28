package com.group2.ADN.repository;

import com.group2.ADN.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("SELECT r FROM Review r JOIN r.ticket t JOIN r.customer c ORDER BY r.createdAt DESC")
    List<Review> findAllByOrderByCreatedAtDesc();
    
    Optional<Review> findByTicketId(Long ticketId);
} 