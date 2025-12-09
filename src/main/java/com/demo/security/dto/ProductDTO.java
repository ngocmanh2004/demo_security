package com.demo.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Integer productID;
    private BigDecimal price;
    private BigDecimal weight;
    private Integer productCategoryID;
    private String categoryName;
    private Integer stockQuantity;
    private String imageUrl;
    private String productName; // translation
}

