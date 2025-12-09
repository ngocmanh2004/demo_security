package com.demo.security.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductTranslationId implements Serializable {

    @Column(name = "ProductID")
    private Integer productID;

    @Column(name = "LanguageID", length = 2)
    private String languageID;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductTranslationId that = (ProductTranslationId) o;
        return Objects.equals(productID, that.productID) &&
                Objects.equals(languageID, that.languageID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productID, languageID);
    }
}