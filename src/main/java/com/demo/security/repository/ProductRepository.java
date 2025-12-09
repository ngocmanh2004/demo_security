package com.demo.security.repository;

import com.demo.security.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByCategory_ProductCategoryID(Integer categoryId);

    // Fetch products with their translations AND category translations
    // This works now because we changed List to Set in the entities
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.translations " +
            "LEFT JOIN FETCH p.category c " +
            "LEFT JOIN FETCH c.translations")
    List<Product> findAllWithTranslation();

    // Fetch single product with all translations
    @Query("SELECT p FROM Product p " +
            "LEFT JOIN FETCH p.translations " +
            "LEFT JOIN FETCH p.category c " +
            "LEFT JOIN FETCH c.translations " +
            "WHERE p.productID = :id")
    Product findByIdWithTranslation(@Param("id") Integer id);
}