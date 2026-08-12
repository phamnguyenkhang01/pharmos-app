package com.pharmos.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.pharmos.category.domain.Category;
import com.pharmos.common.dao.JPAFactory;
import com.pharmos.testsupport.AbstractDaoIT;

import jakarta.persistence.PersistenceException;

public class CategoryServiceTest extends AbstractDaoIT {
    private final CategoryService categoryService = new CategoryService();

    @AfterEach
    void cleanDatabase() {
        JPAFactory.getEmf("com.pharmos.common").runInTransaction(em ->
            em.createNativeQuery("TRUNCATE TABLE category RESTART IDENTITY CASCADE").executeUpdate());
    }

    @Test
    void createAndReadRoundTripsAllFields() {
        Category category = new Category("Pain relief", "Giảm đau", null, 1);

        categoryService.create(category);

        Category found = categoryService.read(category.getId());

        assertThat(found.getNameEn()).isEqualTo("Pain relief");
        assertThat(found.getNameVi()).isEqualTo("Giảm đau");
        assertThat(found.getSortOrder()).isEqualTo(1);
        assertThat(found.getParent()).isNull();
    }

    @Test
    void childReadsBackWithParentAssociationIntact() {
        Category parent = new Category("Medication", "Thuốc", null, 1);
        categoryService.create(parent);

        Category child = new Category("Pain relief", "Giảm đau", parent, 1);
        categoryService.create(child);

        Category foundChild = categoryService.read(child.getId());

        assertThat(foundChild.getParent().getId()).isEqualTo(parent.getId());
    }

    @Test
    void updatePersistsChangesToExistingRow() {
        Category category = new Category("Pain relief", "Giảm đau", null, 1);
        categoryService.create(category);

        category.setNameEn("Pain relief - updated");
        category.setSortOrder(2);
        categoryService.update(category);

        Category found = categoryService.read(category.getId());

        assertThat(found.getNameEn()).isEqualTo("Pain relief - updated");
        assertThat(found.getSortOrder()).isEqualTo(2);
    }

    @Test
    void deleteRemovesRow() {
        Category category = new Category("Pain relief", "Giảm đau", null, 1);
        categoryService.create(category);

        categoryService.delete(category);

        assertThat(categoryService.read(category.getId())).isNull();
    }

    @Test
    void deletingParentWithChildrenViolatesForeignKeyConstraint() {
        Category parent = new Category("Medication", "Thuốc", null, 1);
        categoryService.create(parent);

        Category child = new Category("Pain relief", "Giảm đau", parent, 1);
        categoryService.create(child);

        assertThatThrownBy(() -> categoryService.delete(parent))
                .isInstanceOf(PersistenceException.class);
    }
}
