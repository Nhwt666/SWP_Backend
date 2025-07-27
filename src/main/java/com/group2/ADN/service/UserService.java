package com.group2.ADN.service;

import com.group2.ADN.dto.UpdateProfileRequest;
import com.group2.ADN.entity.User;
import com.group2.ADN.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.group2.ADN.dto.AdminUpdateUserRequest;
import com.group2.ADN.entity.UserRole;
import org.springframework.data.jpa.domain.Specification;
import com.group2.ADN.repository.TicketRepository;
import com.group2.ADN.dto.UserWithTicketStatsDto;
import com.group2.ADN.entity.TicketStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TicketRepository ticketRepository;

 

    /**
     * Cập nhật thông tin profile của user dựa trên email
     */
    public void updateProfile(String email, UpdateProfileRequest request) {
        log.info("==== Update Profile Called ====");
        log.info("Email: {}", email);
        log.info("Request data: {}", request);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("❌ User not found for email: {}", email);
                    return new RuntimeException("User not found");
                });

        if (request.getFullName() != null) {
            log.info("Updating full name to: {}", request.getFullName());
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            log.info("Updating phone to: {}", request.getPhone());
            user.setPhone(request.getPhone());
        }

        if (request.getAddress() != null) {
            log.info("Updating address to: {}", request.getAddress());
            user.setAddress(request.getAddress());
        }

        userRepository.save(user);
        log.info("✅ Cập Nhật Thông Tin Thành Công");
    }

    /**
     * Admin cập nhật thông tin user
     */
    @Transactional
    public User updateUserByAdmin(Long userId, AdminUpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getWalletBalance() != null) {
            user.setWalletBalance(request.getWalletBalance());
        }

        return userRepository.save(user);
    }

    /**
     * Tìm danh sách user kèm thống kê số lượng ticket (dùng cho dashboard)
     */
    public List<UserWithTicketStatsDto> findUsersWithFiltersAndStats(String roleStr, String keyword) {
        // First, get the filtered list of users
        List<User> users = findUsersWithFilters(roleStr, keyword);

        // Then, for each user, calculate stats and map to DTO
        return users.stream().map(user -> {
            if (user.getRole() == UserRole.STAFF) {
                long inProgressCount = ticketRepository.countByStaffAndStatus(user, TicketStatus.IN_PROGRESS);
                long completedCount = ticketRepository.countByStaffAndStatus(user, TicketStatus.COMPLETED);
                return new UserWithTicketStatsDto(user, inProgressCount, completedCount);
            } else {
                // For non-staff users, stats are 0
                return new UserWithTicketStatsDto(user, 0, 0);
            }
        }).collect(Collectors.toList());
    }

    /**
     * Tìm danh sách user theo filter role và keyword
     */
    public List<User> findUsersWithFilters(String roleStr, String keyword) {
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            // Predicate list
            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            // Role filter
            if (roleStr != null && !roleStr.isEmpty() && !roleStr.equalsIgnoreCase("ALL")) {
                try {
                    UserRole role = UserRole.valueOf(roleStr.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("role"), role));
                } catch (IllegalArgumentException e) {
                    // Handle invalid role string if necessary, or just ignore
                }
            }

            // Keyword filter (for fullName or email)
            if (keyword != null && !keyword.trim().isEmpty()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern)
                ));
            }
            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return userRepository.findAll(spec);
    }


    /**
     * Nạp tiền vào ví của user
     */
    @Transactional
    public void topUpWallet(Long userId, BigDecimal amount, String paymentMethod) {
        log.info("🔁 Top-up requested: userId = {}, amount = {}, paymentMethod = {}", userId, amount, paymentMethod);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal currentBalance = user.getWalletBalance();
        if (currentBalance == null) {
            currentBalance = BigDecimal.ZERO;
        }
        user.setWalletBalance(currentBalance.add(amount));
        userRepository.save(user);

        log.info("✅ {} payment successful - Wallet new balance: {}", paymentMethod, user.getWalletBalance());
    }
}
