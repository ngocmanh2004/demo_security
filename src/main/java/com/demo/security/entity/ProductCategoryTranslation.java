package com.demo.security.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ProductCategoryTranslation")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCategoryTranslation {
    @EmbeddedId
    private ProductCategoryTranslationId id;

    @ManyToOne
    @MapsId("productCategoryID")
    @JoinColumn(name = "ProductCategoryID")
    private ProductCategory category;

    @ManyToOne
    @MapsId("languageID")
    @JoinColumn(name = "LanguageID")
    private Language language;

    @Column(nullable = false, length = 100)
    private String categoryName;
}
