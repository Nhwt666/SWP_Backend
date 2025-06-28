package com.group2.ADN.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDTO {
    private Long id;
    private String customerName;
    private Integer rating;
    private String feedback;
    private String createdAt;
    private String ticketId;
} 