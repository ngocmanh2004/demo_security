package com.demo.security.controller;

import com.demo.security.dto.ProductDTO;
import com.demo.security.entity.Language;
import com.demo.security.entity.Product;
import com.demo.security.entity.ProductCategory;
import com.demo.security.mapper.ProductMapper;
import com.demo.security.repository.LanguageRepository;
import com.demo.security.service.ProductCategoryService;
import com.demo.security.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/products")
@RequiredArgsConstructor
public class PublicProductController {

    private final ProductService productService;
    private final ProductCategoryService categoryService;
    private final LanguageRepository languageRepository;

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        Language lang = languageRepository.findById("vi").orElse(null);
        String langId = lang != null ? lang.getLanguageID() : null;
        List<Product> products = productService.getAllProductsWithTranslation(langId);
        List<ProductDTO> dtos = products.stream().map(p -> ProductMapper.toDto(p, langId)).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Integer id) {
        Language lang = languageRepository.findById("vi").orElse(null);
        String langId = lang != null ? lang.getLanguageID() : null;
        Product p = productService.getProductByIdWithTranslation(id, langId);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ProductMapper.toDto(p, langId));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable Integer categoryId) {
        Language lang = languageRepository.findById("vi").orElse(null);
        String langId = lang != null ? lang.getLanguageID() : null;
        List<Product> products = productService.getProductsByCategory(categoryId);
        List<ProductDTO> dtos = products.stream().map(p -> ProductMapper.toDto(p, langId)).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<com.demo.security.dto.ProductCategoryDTO>> getAllCategories() {
        Language lang = languageRepository.findById("vi").orElse(null);
        String langId = lang != null ? lang.getLanguageID() : null;
        List<ProductCategory> categories = categoryService.getAllCategoriesWithTranslation();

        List<com.demo.security.dto.ProductCategoryDTO> dtos = categories.stream().map(c -> {
            String name = null;
            if (c.getTranslations() != null && !c.getTranslations().isEmpty()) {
                // Use stream().findFirst() instead of .get(0) for Set
                name = c.getTranslations().stream()
                        .filter(t -> t.getLanguage() != null && (langId == null || t.getLanguage().getLanguageID().equals(langId)))
                        .map(t -> t.getCategoryName())
                        .findFirst()
                        .orElseGet(() -> c.getTranslations().stream()
                                .map(t -> t.getCategoryName())
                                .findFirst()
                                .orElse(null));
            }
            return com.demo.security.dto.ProductCategoryDTO.builder()
                    .productCategoryID(c.getProductCategoryID())
                    .categoryName(name)
                    .canBeShipped(c.getCanBeShipped())
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}