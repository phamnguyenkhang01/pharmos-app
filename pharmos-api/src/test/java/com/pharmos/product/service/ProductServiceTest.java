package com.pharmos.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pharmos.category.domain.Category;
import com.pharmos.common.dao.JPAFactory;
import com.pharmos.product.domain.Product;
import com.pharmos.product.domain.Status;
import com.pharmos.testsupport.AbstractDaoIT;

public class ProductServiceTest extends AbstractDaoIT {
    private final ProductService productService = new ProductService();

    private Category category;

    @BeforeEach
    void createCategory() {
        category = new Category("Pain Relief", "Giảm đau", null, 1);
        JPAFactory.getEmf("com.pharmos.common").runInTransaction(em -> em.persist(category));
    }

    @AfterEach
    void cleanDatabase() {
        JPAFactory.getEmf("com.pharmos.common").runInTransaction(em ->
            em.createNativeQuery("TRUNCATE TABLE product, category RESTART IDENTITY CASCADE").executeUpdate());
    }

    @Test
    void createAndReadRoundTripsAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Product product = new Product("Paracetamol 500mg", "Paracetamol 500mg", "Pain reliever", "Thuốc giảm đau",
                category, new BigDecimal("12.50"), "paracetamol.jpg", 100, Status.PUBLISHED, true, false, now, now);

        productService.create(product);

        Product found = productService.read(product.getId());

        assertThat(found.getNameEn()).isEqualTo("Paracetamol 500mg");
        assertThat(found.getPrice()).isEqualByComparingTo("12.50");
        assertThat(found.getStockQuantity()).isEqualTo(100);
        assertThat(found.getStatus()).isEqualTo(Status.PUBLISHED);
        assertThat(found.getCategory().getId()).isEqualTo(category.getId());
    }

    @Test
    void readAllReturnsEveryPersistedProduct() {
        LocalDateTime now = LocalDateTime.now();
        productService.create(new Product("Paracetamol 500mg", "Paracetamol 500mg", "Pain reliever", "Thuốc giảm đau",
                category, new BigDecimal("12.50"), "paracetamol.jpg", 100, Status.PUBLISHED, true, false, now, now));
        productService.create(new Product("Vitamin C 1000mg", "Vitamin C 1000mg", "Immune support", "Bổ sung vitamin",
                category, new BigDecimal("8.00"), "vitaminc.jpg", 200, Status.DRAFT, false, false, now, now));

        assertThat(productService.readAll()).hasSize(2);
    }

    @Test
    void updatePersistsChangesToExistingRow() {
        LocalDateTime now = LocalDateTime.now();
        Product product = new Product("Paracetamol 500mg", "Paracetamol 500mg", "Pain reliever", "Thuốc giảm đau",
                category, new BigDecimal("12.50"), "paracetamol.jpg", 100, Status.PUBLISHED, true, false, now, now);
        productService.create(product);

        product.setPrice(new BigDecimal("14.00"));
        product.setStockQuantity(80);
        product.setStatus(Status.UNPUBLISHED);
        productService.update(product);

        Product found = productService.read(product.getId());

        assertThat(found.getPrice()).isEqualByComparingTo("14.00");
        assertThat(found.getStockQuantity()).isEqualTo(80);
        assertThat(found.getStatus()).isEqualTo(Status.UNPUBLISHED);
    }

    @Test
    void deleteRemovesRow() {
        LocalDateTime now = LocalDateTime.now();
        Product product = new Product("Paracetamol 500mg", "Paracetamol 500mg", "Pain reliever", "Thuốc giảm đau",
                category, new BigDecimal("12.50"), "paracetamol.jpg", 100, Status.PUBLISHED, true, false, now, now);
        productService.create(product);

        productService.delete(product);

        assertThat(productService.read(product.getId())).isNull();
    }

    @Test
    void searchReturnsOnlyPublishedProducts() {
        productService.create(product("Paracetamol 500mg", "Paracetamol 500mg", category, new BigDecimal("12.50"), 100, Status.DRAFT));
        Product published = product("Ibuprofen 200mg", "Ibuprofen 200mg", category, new BigDecimal("9.00"), 50, Status.PUBLISHED);
        productService.create(published);
        productService.create(product("Aspirin 100mg", "Aspirin 100mg", category, new BigDecimal("5.00"), 30, Status.UNPUBLISHED));

        List<Product> results = productService.search(null, null, null, null, false, 0, 10);

        assertThat(results).extracting(Product::getId).containsExactly(published.getId());
    }


    @Test
    void searchMatchesKeywordCaseInsensitivelyInEnglishOrVietnameseName() {
        Product paracetamol = product("Paracetamol 500mg", "Thuốc giảm đau Paracetamol", category, new BigDecimal("12.50"), 100, Status.PUBLISHED);
        productService.create(paracetamol);
        productService.create(product("Vitamin C 1000mg", "Vitamin C 1000mg", category, new BigDecimal("8.00"), 200, Status.PUBLISHED));

        List<Product> byEngLishName = productService.search("PARACETAMOL", null, null, null, false, 0, 10);
        List<Product> byVietnameseName = productService.search("giảm đau", null, null, null, false, 0, 10);

        assertThat(byEngLishName).extracting(Product::getId).containsExactly(paracetamol.getId());
        assertThat(byVietnameseName).extracting(Product::getId).containsExactly(paracetamol.getId());
    }

    @Test
    void searchFiltersByCategoryId() {
        Category otherCategory = new Category("Vitamins", "Vitamin", null, 2);
        JPAFactory.getEmf("com.pharmos.common").runInTransaction(em -> em.persist(otherCategory));

        Product painReliever = product("Paracetamol 500mg", "Paracetamol 500mg", category, new BigDecimal("12.50"), 100, Status.PUBLISHED);
        productService.create(painReliever);
        productService.create(product("Vitamin C 1000mg", "Vitamin C 1000mg", otherCategory, new BigDecimal("8.00"), 200, Status.PUBLISHED));

        List<Product> results = productService.search(null, category.getId(), null, null, false, 0, 10);

        assertThat(results).extracting(Product::getId).containsExactly(painReliever.getId());
    }

    @Test
    void searchFiltersByPriceRange() {
        Product cheap = product("Aspirin 100mg", "Aspirin 100mg", category, new BigDecimal("5.00"), 100, Status.PUBLISHED);
        Product midRange = product("Paracetamol 500mg", "Paracetamol 500mg", category, new BigDecimal("12.50"), 100, Status.PUBLISHED);
        Product expensive = product("Vitamin C 1000mg", "Vitamin C 1000mg", category, new BigDecimal("30.00"), 100, Status.PUBLISHED);
        productService.create(cheap);
        productService.create(midRange);
        productService.create(expensive);

        List<Product> results = productService.search(null, null, new BigDecimal("10.00"), new BigDecimal("20.00"), false, 0, 10);

        assertThat(results).extracting(Product::getId).containsExactly(midRange.getId());
    }

    @Test
    void searchInStockOnlyExcludesZeroStockProducts() {
        Product inStock = product("Paracetamol 500mg", "Paracetamol 500mg", category, new BigDecimal("12.50"), 100, Status.PUBLISHED);
        Product outOfStock = product("Vitamin C 1000mg", "Vitamin C 1000mg", category, new BigDecimal("8.00"), 0, Status.PUBLISHED);
        productService.create(inStock);
        productService.create(outOfStock);

        List<Product> results = productService.search(null, null, null, null, true, 0, 10);

        assertThat(results).extracting(Product::getId).containsExactly(inStock.getId());
    }

    @Test
    void searchAppliesPagination() {
        Product first = product("Product A", "Product A", category, new BigDecimal("1.00"), 10, Status.PUBLISHED);
        Product second = product("Product B", "Product B", category, new BigDecimal("2.00"), 10, Status.PUBLISHED);
        Product third = product("Product C", "Product C", category, new BigDecimal("3.00"), 10, Status.PUBLISHED);
        productService.create(first);
        productService.create(second);
        productService.create(third);

        List<Product> firstPage = productService.search(null, null, null, null, false, 0, 2);
        List<Product> secondPage = productService.search(null, null, null, null, false, 1, 2);

        assertThat(firstPage).extracting(Product::getId).containsExactly(first.getId(), second.getId());
        assertThat(secondPage).extracting(Product::getId).containsExactly(third.getId());
    }

    @Test
    void searchRejectsInvalidPageOrSize() {
        assertThatThrownBy(() -> productService.search(null, null, null, null, false, -1, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> productService.search(null, null, null, null, false, 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Product product(String nameEn, String nameVi, Category category, BigDecimal price, int stockQuantity, Status status) {
        LocalDateTime now = LocalDateTime.now();
        return new Product(nameEn, nameVi, "description", "mô tả", category, price, "image.jpg", stockQuantity, status, true, false, now, now);
    }
}
