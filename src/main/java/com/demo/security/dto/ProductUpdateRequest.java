package com.demo.security.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateRequest {
    @NotNull
    private Integer productID;
    @NotNull
    private BigDecimal price;
    private BigDecimal weight;
    @NotNull
    private Integer productCategoryID;
    @Min(0)
    private Integer stockQuantity = 0;
    private String imageUrl;
    private String productName; // optional translation for default language
}

