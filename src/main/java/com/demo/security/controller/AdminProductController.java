package com.demo.security.controller;

import com.demo.security.dto.MessageResponse;
import com.demo.security.dto.ProductCreateRequest;
import com.demo.security.dto.ProductDTO;
import com.demo.security.dto.ProductUpdateRequest;
import com.demo.security.entity.Language;
import com.demo.security.entity.Product;
import com.demo.security.entity.ProductCategory;
import com.demo.security.mapper.ProductMapper;
import com.demo.security.repository.LanguageRepository;
import com.demo.security.service.ProductCategoryService;
import com.demo.security.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.demo.security.entity.ProductTranslation;
import com.demo.security.entity.ProductTranslationId;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final ProductCategoryService categoryService;
    private final LanguageRepository languageRepository;

    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductCreateRequest req) {
        try {
            ProductCategory category = categoryService.getCategoryById(req.getProductCategoryID()).orElse(null);
            if (category == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(MessageResponse.builder().message("Invalid category ID").status(400).build());
            }
            Language lang = languageRepository.findById("vi").orElse(null);

            // Create product WITHOUT translation first
            Product p = new Product();
            p.setPrice(req.getPrice());
            p.setWeight(req.getWeight());
            p.setCategory(category);
            p.setStockQuantity(req.getStockQuantity());
            p.setImageUrl(req.getImageUrl());

            // Save to get ID
            Product saved = productService.saveProduct(p);

            // NOW add translation with proper ID
            if (req.getProductName() != null && lang != null) {
                ProductTranslationId translationId = new ProductTranslationId(saved.getProductID(), lang.getLanguageID());
                ProductTranslation tr = ProductTranslation.builder()
                        .id(translationId)
                        .product(saved)
                        .language(lang)
                        .productName(req.getProductName())
                        .build();
                saved.getTranslations().add(tr);
                saved = productService.saveProduct(saved);
            }

            ProductDTO dto = ProductMapper.toDto(saved, lang != null ? lang.getLanguageID() : null);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.builder()
                            .message("Failed to create product: " + e.getMessage())
                            .status(500)
                            .build());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Integer id, @Valid @RequestBody ProductUpdateRequest req) {
        try {
            return productService.getProductById(id)
                    .map(existingProduct -> {
                        ProductCategory category = categoryService.getCategoryById(req.getProductCategoryID()).orElse(null);
                        if (category == null) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                    .body(MessageResponse.builder().message("Invalid category ID").status(400).build());
                        }
                        Language lang = languageRepository.findById("vi").orElse(null);

                        ProductMapper.applyUpdateRequest(existingProduct, req, category, lang);
                        Product saved = productService.saveProduct(existingProduct);
                        ProductDTO dto = ProductMapper.toDto(saved, lang != null ? lang.getLanguageID() : null);
                        return ResponseEntity.ok(dto);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.builder()
                            .message("Failed to update product: " + e.getMessage())
                            .status(500)
                            .build());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {
        try {
            return productService.getProductById(id)
                    .map(product -> {
                        productService.deleteProduct(id);
                        return ResponseEntity.ok(MessageResponse.builder()
                                .message("Product deleted successfully")
                                .status(200)
                                .build());
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.builder()
                            .message("Failed to delete product: " + e.getMessage())
                            .status(500)
                            .build());
        }
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> listAll() {
        Language lang = languageRepository.findById("vi").orElse(null);
        String langId = lang != null ? lang.getLanguageID() : null;
        // Use translation-aware fetch to ensure translations and category translations are loaded
        List<Product> products = productService.getAllProductsWithTranslation(langId);
        List<ProductDTO> dtos = products.stream().map(p -> ProductMapper.toDto(p, langId)).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}