package com.group2.ADN.repository;

import com.group2.ADN.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogRepository extends JpaRepository<Blog, Long> {
} 