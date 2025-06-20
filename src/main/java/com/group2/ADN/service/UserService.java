package com.group2.ADN.service;

import com.group2.ADN.dto.UpdateProfileRequest;
import com.group2.ADN.entity.User;
import com.group2.ADN.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void topUpWallet(Long userId, BigDecimal amount, String paymentMethod) {
        System.out.println("🔁 Top-up requested: userId = " + userId + ", amount = " + amount);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal currentBalance = user.getWalletBalance();
        if (currentBalance == null) {
            currentBalance = BigDecimal.ZERO;
        }
        if ("PAYPAL".equals(paymentMethod)) {
            BigDecimal vndAmount = amount.multiply(BigDecimal.valueOf(26000));
            user.setWalletBalance(currentBalance.add(vndAmount));
            System.out.println("✅ Cộng tiền PayPal: " + amount + " USD => " + vndAmount + " VNĐ");
        } else if ("PAYPAL_SUCCESS".equals(paymentMethod)) {
            // This case is for when the amount is already converted to VND in the controller
            user.setWalletBalance(currentBalance.add(amount));
            System.out.println("✅ Cộng tiền PayPal (đã quy đổi): " + amount + " VNĐ");
        } else {
            user.setWalletBalance(currentBalance.add(amount));
        }
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


}
