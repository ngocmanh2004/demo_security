package com.demo.security.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ProductCategory")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productCategoryID;

    @Column(nullable = false)
    private Boolean canBeShipped = true;

    // Changed from List to Set to avoid MultipleBagFetchException
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductCategoryTranslation> translations = new HashSet<>();

    // Changed from List to Set
    @OneToMany(mappedBy = "category")
    private Set<Product> products = new HashSet<>();
}