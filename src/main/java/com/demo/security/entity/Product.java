package com.demo.security.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Product")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productID;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 6, scale = 2)
    private BigDecimal weight;

    @ManyToOne
    @JoinColumn(name = "ProductCategoryID", nullable = false)
    private ProductCategory category;

    private Integer stockQuantity = 0;

    private String imageUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    private LocalDateTime updatedDate = LocalDateTime.now();

    // Changed from List to Set to avoid MultipleBagFetchException
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductTranslation> translations = new HashSet<>();

    @PreUpdate
    public void preUpdate() {
        updatedDate = LocalDateTime.now();
    }
}