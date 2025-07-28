package com.group2.ADN.dto;

import com.group2.ADN.entity.PriceType;
import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class AdminCreatePriceRequest {
    @NotNull(message = "Giá trị là bắt buộc")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị phải lớn hơn 0")
    private BigDecimal value;

    @NotBlank(message = "Đơn vị tiền tệ là bắt buộc")
    private String currency;

    @NotBlank(message = "Tên là bắt buộc")
    @Size(max = 255, message = "Tên tối đa 255 ký tự")
    private String name;

    @NotNull(message = "Loại là bắt buộc")
    private PriceType type;
} 