package com.demo.security.mapper;

import com.demo.security.dto.ProductCreateRequest;
import com.demo.security.dto.ProductDTO;
import com.demo.security.dto.ProductUpdateRequest;
import com.demo.security.entity.Product;
import com.demo.security.entity.ProductCategory;
import com.demo.security.entity.ProductTranslation;
import com.demo.security.entity.Language;

import java.util.Optional;

public class ProductMapper {

    public static ProductDTO toDto(Product p, String languageId) {
        if (p == null) return null;

        ProductDTO dto = ProductDTO.builder()
                .productID(p.getProductID())
                .price(p.getPrice())
                .weight(p.getWeight())
                .productCategoryID(p.getCategory() != null ? p.getCategory().getProductCategoryID() : null)
                .stockQuantity(p.getStockQuantity())
                .imageUrl(p.getImageUrl())
                .build();

        // Get category name from translations (Set instead of List)
        if (p.getCategory() != null && p.getCategory().getTranslations() != null && !p.getCategory().getTranslations().isEmpty()) {
            String categoryName = p.getCategory().getTranslations().stream()
                    .filter(t -> t.getLanguage() != null && (languageId == null || t.getLanguage().getLanguageID().equals(languageId)))
                    .map(t -> t.getCategoryName())
                    .findFirst()
                    .orElseGet(() -> p.getCategory().getTranslations().stream()
                            .map(t -> t.getCategoryName())
                            .findFirst()
                            .orElse(null));
            dto.setCategoryName(categoryName);
        }

        // Get product name from translations (Set instead of List)
        if (languageId != null && p.getTranslations() != null && !p.getTranslations().isEmpty()) {
            Optional<ProductTranslation> tr = p.getTranslations().stream()
                    .filter(t -> t.getId() != null && t.getId().getLanguageID().equals(languageId))
                    .findFirst();
            tr.ifPresent(t -> dto.setProductName(t.getProductName()));
        } else if (p.getTranslations() != null && !p.getTranslations().isEmpty()) {
            p.getTranslations().stream()
                    .map(ProductTranslation::getProductName)
                    .findFirst()
                    .ifPresent(dto::setProductName);
        }

        return dto;
    }

    public static Product fromCreateRequest(ProductCreateRequest r, ProductCategory category, Language language) {
        Product p = new Product();
        p.setPrice(r.getPrice());
        p.setWeight(r.getWeight());
        p.setCategory(category);
        p.setStockQuantity(r.getStockQuantity());
        p.setImageUrl(r.getImageUrl());

        if (r.getProductName() != null && language != null) {
            ProductTranslation tr = ProductTranslation.builder()
                    .id(null)
                    .product(p)
                    .language(language)
                    .productName(r.getProductName())
                    .build();
            p.getTranslations().add(tr);
        }
        return p;
    }

    public static void applyUpdateRequest(Product p, ProductUpdateRequest r, ProductCategory category, Language language) {
        p.setPrice(r.getPrice());
        p.setWeight(r.getWeight());
        p.setCategory(category);
        p.setStockQuantity(r.getStockQuantity());
        p.setImageUrl(r.getImageUrl());

        if (r.getProductName() != null && language != null) {
            // Find existing translation for the language
            Optional<ProductTranslation> existing = p.getTranslations().stream()
                    .filter(t -> t.getLanguage() != null && t.getLanguage().getLanguageID().equals(language.getLanguageID()))
                    .findFirst();

            if (existing.isPresent()) {
                existing.get().setProductName(r.getProductName());
            } else {
                ProductTranslation tr = ProductTranslation.builder()
                        .id(null)
                        .product(p)
                        .language(language)
                        .productName(r.getProductName())
                        .build();
                p.getTranslations().add(tr);
            }
        }
    }
}