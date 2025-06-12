package com.group2.ADN.service;

import com.group2.ADN.entity.User;
import com.group2.ADN.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void topUpWallet(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setWalletBalance(user.getWalletBalance().add(amount));
        userRepository.save(user);
    }
}
