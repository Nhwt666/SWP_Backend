package com.group2.ADN.controller;

import com.group2.ADN.entity.Blog;
import com.group2.ADN.service.BlogService;
import com.group2.ADN.service.ImgBBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import java.util.Map;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {
    @Autowired
    private BlogService blogService;
    
    @Autowired
    private ImgBBService imgBBService;

    @GetMapping
    public List<Blog> getAllBlogs() {
        return blogService.getAllBlogs();
    }

    // Public: Get a single blog by ID
    @GetMapping("/{id}")
    public Blog getBlogById(@PathVariable Long id) {
        return blogService.getBlogById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Blog createBlog(@RequestBody Blog blog) {
        return blogService.saveBlog(blog);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadImageToImgBB(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (!imgBBService.isValidImageFile(file)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid image file"));
            }
            
            // Upload image to ImgBB
            String imageUrl = imgBBService.uploadImage(file);
            return ResponseEntity.ok(Map.of("url", imageUrl));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error uploading image: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Blog> updateBlog(@PathVariable Long id, @RequestBody Blog blog) {
        Blog updatedBlog = blogService.updateBlog(id, blog);
        return ResponseEntity.ok(updatedBlog);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return ResponseEntity.noContent().build();
    }
} 