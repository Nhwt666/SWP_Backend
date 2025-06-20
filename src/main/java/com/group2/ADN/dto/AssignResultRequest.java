package com.group2.ADN.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignResultRequest {
    @NotNull
    private Long ticketId;

    @NotNull
    @Min(0)
    @Max(100)
    private Double percentage;

    private String description;
} 