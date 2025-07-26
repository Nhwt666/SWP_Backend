package com.group2.ADN.service;

import com.group2.ADN.entity.Blog;
import com.group2.ADN.repository.BlogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlogService {
    @Autowired
    private BlogRepository blogRepository;

    public Blog saveBlog(Blog blog) {
        return blogRepository.save(blog);
    }

    public List<Blog> getAllBlogs() {
        return blogRepository.findAll();
    }

    public Blog getBlogById(Long id) {
        return blogRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Blog not found"));
    }

    public void deleteBlog(Long id) {
        if (!blogRepository.existsById(id)) {
            throw new RuntimeException("Blog not found");
        }
        blogRepository.deleteById(id);
    }

    public Blog updateBlog(Long id, Blog blog) {
        Blog existing = getBlogById(id);
        existing.setTitle(blog.getTitle());
        existing.setContent(blog.getContent());
        existing.setImageUrl(blog.getImageUrl());
        return blogRepository.save(existing);
    }
} 