package com.demo.security.config;

import com.demo.security.entity.*;
import com.demo.security.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Seed initial data so the application can be used immediately.
 * Users: admin/admin@example.com (ROLE_ADMIN), user1/user1@example.com (ROLE_USER) with password "123456".
 * Products: two sample electronics items.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initLanguages();

        Role adminRole = ensureRole("ROLE_ADMIN", "Administrator role");
        Role userRole = ensureRole("ROLE_USER", "Standard user role");

        ensureUser("admin", "123456", "admin@example.com", "Administrator", adminRole);
        ensureUser("user1", "123456", "user1@example.com", "User One", userRole);

        if (productRepository.count() == 0) {
            log.info("Seeding demo products");
            ProductCategory electronics = createCategory("Điện tử", "Electronics");
            createProduct(electronics,
                    "Laptop Dell XPS 13", "Dell XPS 13 Laptop",
                    new BigDecimal("15000000"), new BigDecimal("0.50"), 10,
                    "https://via.placeholder.com/300x200?text=Laptop");
            createProduct(electronics,
                    "iPhone 15 Pro", "iPhone 15 Pro",
                    new BigDecimal("18000000"), new BigDecimal("0.20"), 25,
                    "https://via.placeholder.com/300x200?text=Phone");
        }
    }

    private void initLanguages() {
        if (languageRepository.count() == 0) {
            log.info("Seeding languages");
            languageRepository.save(new Language("vi", "Tiếng Việt"));
            languageRepository.save(new Language("en", "English"));
        }
    }

    private Role ensureRole(String roleName, String description) {
        return roleRepository.findByRoleName(roleName)
                .orElseGet(() -> {
                    log.info("Creating role {}", roleName);
                    Role role = Role.builder()
                            .roleName(roleName)
                            .description(description)
                            .build();
                    return roleRepository.save(role);
                });
    }

    private void ensureUser(String username, String rawPassword, String email, String fullName, Role role) {
        Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isPresent()) {
            return;
        }

        log.info("Creating user {}", username);
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .email(email)
                .fullName(fullName)
                .enabled(true)
                .roles(new HashSet<>(Set.of(role)))
                .build();
        userRepository.save(user);
    }

    private ProductCategory createCategory(String nameVi, String nameEn) {
        Language vi = languageRepository.findById("vi").orElseThrow();
        Language en = languageRepository.findById("en").orElseThrow();

        ProductCategory category = ProductCategory.builder()
                .canBeShipped(true)
                .build();

        // Save category FIRST to get ID
        category = categoryRepository.save(category);

        // NOW create translations with proper ID
        ProductCategoryTranslation viTrans = ProductCategoryTranslation.builder()
                .id(new ProductCategoryTranslationId(category.getProductCategoryID(), "vi"))
                .category(category)
                .language(vi)
                .categoryName(nameVi)
                .build();

        ProductCategoryTranslation enTrans = ProductCategoryTranslation.builder()
                .id(new ProductCategoryTranslationId(category.getProductCategoryID(), "en"))
                .category(category)
                .language(en)
                .categoryName(nameEn)
                .build();

        category.setTranslations(new HashSet<>(Arrays.asList(viTrans, enTrans)));
        return categoryRepository.save(category);
    }

    private Product createProduct(ProductCategory category,
                                  String nameVi,
                                  String nameEn,
                                  BigDecimal price,
                                  BigDecimal weight,
                                  int stock,
                                  String imageUrl) {
        Language vi = languageRepository.findById("vi").orElseThrow();
        Language en = languageRepository.findById("en").orElseThrow();

        Product product = Product.builder()
                .category(category)
                .price(price)
                .weight(weight)
                .stockQuantity(stock)
                .imageUrl(imageUrl)
                .build();

        // Save product FIRST to get ID
        product = productRepository.save(product);

        // NOW create translations with proper ID
        ProductTranslation viTrans = ProductTranslation.builder()
                .id(new ProductTranslationId(product.getProductID(), "vi"))
                .product(product)
                .language(vi)
                .productName(nameVi)
                .description(nameVi)
                .build();

        ProductTranslation enTrans = ProductTranslation.builder()
                .id(new ProductTranslationId(product.getProductID(), "en"))
                .product(product)
                .language(en)
                .productName(nameEn)
                .description(nameEn)
                .build();

        product.setTranslations(new HashSet<>(Arrays.asList(viTrans, enTrans)));
        return productRepository.save(product);
    }
}