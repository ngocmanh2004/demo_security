package com.demo.security.service;

import com.demo.security.entity.Product;
import com.demo.security.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProductsWithTranslation(String languageId) {
        // Method name changed from findAllWithTranslations() to findAllWithTranslation()
        return productRepository.findAllWithTranslation();
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Integer id) {
        return productRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Product getProductByIdWithTranslation(Integer id, String languageId) {
        // Method name changed from findByIdWithTranslations() to findByIdWithTranslation()
        return productRepository.findByIdWithTranslation(id);
    }

    @Transactional
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Integer id) {
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(Integer categoryId) {
        return productRepository.findByCategory_ProductCategoryID(categoryId);
    }
}