package com.pharmos.category.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

public class CategoryTest {
    private static Validator validator;

    private Category parent;
    private Category category;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @BeforeEach
    void setUp() {
        parent = new Category("Medications", "Thuốc", null, 1);
        category = new Category("Pain Relief", "Giảm đau", parent, 2);
    }

    @Test
    void constructorSetsAllFields() {
        assertThat(category.getNameEn()).isEqualTo("Pain Relief");
        assertThat(category.getNameVi()).isEqualTo("Giảm đau");
        assertThat(category.getParent()).isEqualTo(parent);
        assertThat(category.getSortOrder()).isEqualTo(2);
        assertThat(category.getId()).isNull();
    }

    @Test
    void noArgConstructorLeavesChildrenEmptyNotNull() {
        Category blank = new Category();

        assertThat(blank.getChildren()).isNotNull();
        assertThat(blank.getChildren()).isEmpty();
    }

    @Test
    void settersUpdateFields() {
        Category newParent = new Category("Skincare", "Chăm sóc da", null, 3);

        category.setNameEn("Allergy Relief");
        category.setNameVi("Giảm dị ứng");
        category.setParent(newParent);
        category.setSortOrder(4);

        assertThat(category.getNameEn()).isEqualTo("Allergy Relief");
        assertThat(category.getNameVi()).isEqualTo("Giảm dị ứng");
        assertThat(category.getParent()).isEqualTo(newParent);
        assertThat(category.getSortOrder()).isEqualTo(4);
    }

    @Test
    void validCategoryHasNoViolations() {
        Set<ConstraintViolation<Category>> violations = validator.validate(category);

        assertThat(violations).isEmpty();
    }

    @Test
    void topLevelCategoryWithNullParentHasNoViolations() {
        category.setParent(null);

        assertThat(validator.validate(category)).isEmpty();
    }

    @Test
    void validationFailsWhenNameEnIsNull() {
        category.setNameEn(null);

        assertThat(propertyPaths(validator.validate(category))).contains("nameEn");
    }

    private static List<String> propertyPaths(Set<ConstraintViolation<Category>> violations) {
        return violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();
    }
}