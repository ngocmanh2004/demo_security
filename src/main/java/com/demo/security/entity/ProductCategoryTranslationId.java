package com.demo.security.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProductCategoryTranslationId implements Serializable {
    @Column(name = "ProductCategoryID")
    private Integer productCategoryID;

    @Column(name = "LanguageID", length = 2)
    private String languageID;
}
