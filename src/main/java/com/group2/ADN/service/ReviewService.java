package com.group2.ADN.service;

import com.group2.ADN.dto.ReviewDTO;
import com.group2.ADN.dto.FeedbackRequest;
import com.group2.ADN.entity.Review;
import com.group2.ADN.entity.Ticket;
import com.group2.ADN.entity.User;
import com.group2.ADN.entity.TicketStatus;
import com.group2.ADN.repository.ReviewRepository;
import com.group2.ADN.repository.TicketRepository;
import com.group2.ADN.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public List<ReviewDTO> findAllReviews() {
        List<Review> reviews = reviewRepository.findAllByOrderByCreatedAtDesc();
        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void submitFeedback(Long ticketId, FeedbackRequest request, String customerEmail) {
        // Kiểm tra ticket có tồn tại và thuộc về user hiện tại
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        if (!ticket.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("Ticket does not belong to this customer");
        }
        
        // Kiểm tra ticket đã hoàn thành
        if (ticket.getStatus() != TicketStatus.COMPLETED) {
            throw new RuntimeException("Only completed tickets can be reviewed");
        }
        
        // Kiểm tra chưa có feedback cho ticket này
        if (reviewRepository.findByTicketId(ticketId).isPresent()) {
            throw new RuntimeException("Ticket already has a review");
        }
        
        // Validation rating
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }
        
        // Validation feedback
        if (request.getFeedback() != null && request.getFeedback().length() > 500) {
            throw new RuntimeException("Feedback must not exceed 500 characters");
        }
        
        // Lưu feedback vào bảng reviews
        Review review = Review.builder()
                .ticket(ticket)
                .customer(customer)
                .rating(request.getRating())
                .feedback(request.getFeedback())
                .build();
        
        reviewRepository.save(review);
    }

    private ReviewDTO convertToDTO(Review review) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        
        return ReviewDTO.builder()
                .id(review.getId())
                .customerName(review.getCustomer().getFullName())
                .rating(review.getRating())
                .feedback(review.getFeedback())
                .createdAt(review.getCreatedAt().format(formatter))
                .ticketId("TK" + String.format("%03d", review.getTicket().getId()))
                .build();
    }
} 