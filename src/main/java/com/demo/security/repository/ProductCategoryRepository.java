package com.demo.security.repository;

import com.demo.security.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Integer> {
    @Query("SELECT DISTINCT c FROM ProductCategory c " +
            "LEFT JOIN FETCH c.translations t")
    List<ProductCategory> findAllWithTranslation();
}
