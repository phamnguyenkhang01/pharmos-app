package com.pharmos.category.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.pharmos.category.domain.Category;
import com.pharmos.common.dao.JPAFactory;
import com.pharmos.testsupport.AbstractDaoIT;

import jakarta.persistence.PersistenceException;

public class CategoryDaoTest extends AbstractDaoIT {
    private final CategoryDao categoryDao = new CategoryDao();

    @AfterEach
    void cleanDatabase() {
        JPAFactory.getEmf("com.pharmos.common").runInTransaction(em ->
            em.createNativeQuery("TRUNCATE TABLE category RESTART IDENTITY CASCADE").executeUpdate());
    }

    @Test
    void createAndReadRoundTripsAllFields() {
        Category category = new Category("Pain relief", "Giảm đau", null, 1);
        categoryDao.create(category);

        Category found = categoryDao.read(category.getId());

        assertThat(found.getNameEn()).isEqualTo("Pain relief");
        assertThat(found.getNameVi()).isEqualTo("Giảm đau");
        assertThat(found.getSortOrder()).isEqualTo(1);
        assertThat(found.getParent()).isNull();
    }

    @Test
    void childReadsBackWithParentAssociationIntact() {
        Category parent = new Category("Medication", "Thuốc", null, 1);
        categoryDao.create(parent);

        Category child = new Category("Pain relief", "Giảm đau", parent, 1);
        categoryDao.create(child);

        Category foundChild = categoryDao.read(child.getId());

        assertThat(foundChild.getParent().getId()).isEqualTo(parent.getId());
    }

    @Test
void updatePersistsChangesToExistingRow() {
    Category category = new Category("Pain relief", "Giảm đau", null, 1);
    categoryDao.create(category);

    category.setNameEn("Pain relief - updated");
    category.setSortOrder(2);
    categoryDao.update(category);

    Category found = categoryDao.read(category.getId());

    assertThat(found.getNameEn()).isEqualTo("Pain relief - updated");
    assertThat(found.getSortOrder()).isEqualTo(2);
}


        @Test
    void deletingParentWithChildrenViolatesForeignKeyConstraint() {
        Category parent = new Category("Medication", "Thuốc", null, 1);
        categoryDao.create(parent);

        Category child = new Category("Pain relief", "Giảm đau", parent, 1);
        categoryDao.create(child);

        assertThatThrownBy(() -> categoryDao.delete(parent))
                .isInstanceOf(PersistenceException.class);
    }
}
