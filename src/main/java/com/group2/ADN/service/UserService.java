package com.group2.ADN.service;

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
    public void topUpWallet(Long userId, BigDecimal amount) {
        System.out.println("🔁 Top-up requested: userId = " + userId + ", amount = " + amount);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setWalletBalance(user.getWalletBalance().add(amount));
        userRepository.save(user);

        System.out.println("✅ Wallet new balance: " + user.getWalletBalance());
    }
}
