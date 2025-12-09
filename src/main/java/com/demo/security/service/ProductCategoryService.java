package com.demo.security.service;

import com.demo.security.entity.ProductCategory;
import com.demo.security.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<ProductCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ProductCategory> getAllCategoriesWithTranslation() {
        return categoryRepository.findAllWithTranslation();
    }

    @Transactional(readOnly = true)
    public Optional<ProductCategory> getCategoryById(Integer id) {
        return categoryRepository.findById(id);
    }

    @Transactional
    public ProductCategory saveCategory(ProductCategory category) {
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Integer id) {
        categoryRepository.deleteById(id);
    }
}
