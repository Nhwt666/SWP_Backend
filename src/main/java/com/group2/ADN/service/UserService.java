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

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TicketRepository ticketRepository;

    @Transactional
    public void topUpWallet(Long userId, BigDecimal amount, String paymentMethod) {
        System.out.println("🔁 Top-up requested: userId = " + userId + ", amount = " + amount);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal currentBalance = user.getWalletBalance();
        if (currentBalance == null) {
            currentBalance = BigDecimal.ZERO;
        }
        user.setWalletBalance(currentBalance.add(amount));
        userRepository.save(user);

        System.out.println("✅ Wallet new balance: " + user.getWalletBalance());
    }

    public void updateProfile(String email, UpdateProfileRequest request) {
        System.out.println("==== Update Profile Called ====");
        System.out.println("Email: " + email);
        System.out.println("Request data: " + request);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("❌ User not found for email: " + email);
                    return new RuntimeException("User not found");
                });

        if (request.getFullName() != null) {
            System.out.println("Updating full name to: " + request.getFullName());
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            System.out.println("Updating phone to: " + request.getPhone());
            user.setPhone(request.getPhone());
        }

        if (request.getAddress() != null) {
            System.out.println("Updating address to: " + request.getAddress());
            user.setAddress(request.getAddress());
        }

        userRepository.save(user);
        System.out.println("✅ Cập Nhật Thông Tin Thành Công");
    }

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
}
