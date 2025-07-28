package com.group2.ADN.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
public class ImgBBService {

    @Value("${imgbb.api.key}")
    private String imgbbApiKey;

    @Value("${imgbb.api.url}")
    private String imgbbApiUrl;

    private final RestTemplate restTemplate;

    public ImgBBService() {
        this.restTemplate = new RestTemplate();
    }

    public String uploadImage(MultipartFile file) throws IOException {
        byte[] fileBytes = file.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(fileBytes);
        

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("key", imgbbApiKey);
        body.add("image", base64Image);
        body.add("name", file.getOriginalFilename());

        Map<String, Object> response = restTemplate.postForObject(imgbbApiUrl, body, Map.class);
        
        if (response != null && response.containsKey("data")) {
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            return (String) data.get("url");
        } else {
            throw new RuntimeException("Failed to upload image to ImgBB");
        }
    }

    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
} 