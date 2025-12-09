package com.demo.security.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ProductTranslation")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductTranslation {

    @EmbeddedId
    private ProductTranslationId id;

    @ManyToOne
    @MapsId("productID")
    @JoinColumn(name = "ProductID")
    private Product product;

    @ManyToOne
    @MapsId("languageID")
    @JoinColumn(name = "LanguageID")
    private Language language;

    @Column(name = "ProductName", nullable = false, length = 200)
    private String productName;

    @Column(name = "ProductDescription", columnDefinition = "TEXT")
    private String description;
}