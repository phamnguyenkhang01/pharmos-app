package com.pharmos.product.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pharmos.category.domain.Category;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

public class ProductTest {
    private static Validator validator;

    private Category category;
    private Product product;
    private LocalDateTime now;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @BeforeEach
    void setUp() {
        category = new Category("Pain Relief", "Giảm đau", null, 1);
        now = LocalDateTime.now();
        product = new Product("Paracetamol 500mg", "Paracetamol 500mg", "Pain reliever", "Thuốc giảm đau", category,
                new BigDecimal("12.50"), "paracetamol.jpg", 100, Status.PUBLISHED, true, false, now, now);
    }

    @Test
    void constructorSetsAllFields() {
        assertThat(product.getNameEn()).isEqualTo("Paracetamol 500mg");
        assertThat(product.getNameVi()).isEqualTo("Paracetamol 500mg");
        assertThat(product.getDescriptionEn()).isEqualTo("Pain reliever");
        assertThat(product.getDescriptionVi()).isEqualTo("Thuốc giảm đau");
        assertThat(product.getCategory()).isEqualTo(category);
        assertThat(product.getPrice()).isEqualByComparingTo("12.50");
        assertThat(product.getImageRef()).isEqualTo("paracetamol.jpg");
        assertThat(product.getStockQuantity()).isEqualTo(100);
        assertThat(product.getStatus()).isEqualTo(Status.PUBLISHED);
        assertThat(product.isMedication()).isTrue();
        assertThat(product.isRestricted()).isFalse();
        assertThat(product.getCreatedAt()).isEqualTo(now);
        assertThat(product.getUpdatedAt()).isEqualTo(now);
        assertThat(product.getId()).isNull();
    }

    @Test
    void noArgConstructorLeavesFieldsAtDefaults() {
        Product blank = new Product();

        assertThat(blank.getNameEn()).isNull();
        assertThat(blank.getCategory()).isNull();
        assertThat(blank.getStockQuantity()).isZero();
        assertThat(blank.isMedication()).isFalse();
        assertThat(blank.isRestricted()).isFalse();
    }

    @Test
    void settersUpdateFields() {
        Category newCategory = new Category("Vitamins", "Vitamin", null, 2);

        product.setNameEn("Paracetamol 650mg");
        product.setCategory(newCategory);
        product.setPrice(new BigDecimal("15.00"));
        product.setStockQuantity(50);
        product.setStatus(Status.DRAFT);
        product.setMedication(false);
        product.setRestricted(true);

        assertThat(product.getNameEn()).isEqualTo("Paracetamol 650mg");
        assertThat(product.getCategory()).isEqualTo(newCategory);
        assertThat(product.getPrice()).isEqualByComparingTo("15.00");
        assertThat(product.getStockQuantity()).isEqualTo(50);
        assertThat(product.getStatus()).isEqualTo(Status.DRAFT);
        assertThat(product.isMedication()).isFalse();
        assertThat(product.isRestricted()).isTrue();
    }

    @Test
    void validProductHasNoViolations() {
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        assertThat(violations).isEmpty();
    }

    @Test
    void validationFailsWhenNameEnIsNull() {
        product.setNameEn(null);

        assertThat(propertyPaths(validator.validate(product))).contains("nameEn");
    }

    @Test
    void validationFailsWhenPriceIsNull() {
        product.setPrice(null);

        assertThat(propertyPaths(validator.validate(product))).contains("price");
    }

    @Test
    void validationFailsWhenCreatedAtIsNull() {
        product.setCreatedAt(null);

        assertThat(propertyPaths(validator.validate(product))).contains("createdAt");
    }

    @Test
    void validationFailsWhenUpdatedAtIsNull() {
        product.setUpdatedAt(null);

        assertThat(propertyPaths(validator.validate(product))).contains("updatedAt");
    }

    @Test
    void validationFailsWhenStockQuantityIsNegative() {
        product.setStockQuantity(-1);

        assertThat(propertyPaths(validator.validate(product))).contains("stockQuantity");
    }

    private static List<String> propertyPaths(Set<ConstraintViolation<Product>> violations) {
        return violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();
    }
}
