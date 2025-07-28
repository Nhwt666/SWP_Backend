package com.group2.ADN.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlogRequest {
    @NotBlank(message = "Tiêu đề là bắt buộc")
    @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự")
    private String title;

    @NotBlank(message = "Nội dung là bắt buộc")
    private String content;

    private String imageUrl;
} 