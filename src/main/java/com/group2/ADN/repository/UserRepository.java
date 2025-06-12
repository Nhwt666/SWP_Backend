package com.group2.ADN.repository;


import com.group2.ADN.entity.User;
import com.group2.ADN.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findByRole(UserRole role);

    boolean existsByEmail(String email);
}