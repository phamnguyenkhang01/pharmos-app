package com.pharmos.product.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.pharmos.category.domain.Category;
import com.pharmos.common.dao.JPAFactory;
import com.pharmos.testsupport.AbstractDaoIT;

public class ProductJpaMappingTest extends AbstractDaoIT {
    private final jakarta.persistence.EntityManagerFactory emf = JPAFactory.getEmf("com.pharmos.common");

    @AfterEach
    void cleanDatabase() {
        emf.runInTransaction(em ->
            em.createNativeQuery("TRUNCATE TABLE product, category RESTART IDENTITY CASCADE").executeUpdate());
    }

    private Category persistCategory(jakarta.persistence.EntityManager em) {
        Category category = new Category("Pain Relief", "Giảm đau", null, 1);
        em.persist(category);
        return category;
    }

    @Test
    void productPersistsAndLoadsThroughEntityManager() {
        LocalDateTime now = LocalDateTime.now();
        Category[] category = new Category[1];
        Product product = new Product("Paracetamol 500mg", "Paracetamol 500mg", "Pain reliever", "Thuốc giảm đau",
                null, new BigDecimal("12.50"), "paracetamol.jpg", 100, Status.PUBLISHED, true, false, now, now);

        emf.runInTransaction(em -> {
            category[0] = persistCategory(em);
            product.setCategory(category[0]);
            em.persist(product);
        });

        Product found = emf.callInTransaction(em -> em.find(Product.class, product.getId()));

        assertThat(found.getNameEn()).isEqualTo("Paracetamol 500mg");
        assertThat(found.getPrice()).isEqualByComparingTo("12.50");
        assertThat(found.getStatus()).isEqualTo(Status.PUBLISHED);
        assertThat(found.getCategory().getId()).isEqualTo(category[0].getId());
    }

    @Test
    void productUpdatesThroughEntityManager() {
        LocalDateTime now = LocalDateTime.now();
        Product product = new Product("Paracetamol 500mg", "Paracetamol 500mg", "Pain reliever", "Thuốc giảm đau",
                null, new BigDecimal("12.50"), "paracetamol.jpg", 100, Status.PUBLISHED, true, false, now, now);

        emf.runInTransaction(em -> {
            product.setCategory(persistCategory(em));
            em.persist(product);
        });

        emf.runInTransaction(em -> {
            Product managed = em.find(Product.class, product.getId());
            managed.setStatus(Status.UNPUBLISHED);
            managed.setStockQuantity(80);
        });

        Product updated = emf.callInTransaction(em -> em.find(Product.class, product.getId()));

        assertThat(updated.getStatus()).isEqualTo(Status.UNPUBLISHED);
        assertThat(updated.getStockQuantity()).isEqualTo(80);
    }

    @Test
    void productDeletesThroughEntityManager() {
        LocalDateTime now = LocalDateTime.now();
        Product product = new Product("Paracetamol 500mg", "Paracetamol 500mg", "Pain reliever", "Thuốc giảm đau",
                null, new BigDecimal("12.50"), "paracetamol.jpg", 100, Status.PUBLISHED, true, false, now, now);

        emf.runInTransaction(em -> {
            product.setCategory(persistCategory(em));
            em.persist(product);
        });

        emf.runInTransaction(em -> {
            Product managed = em.find(Product.class, product.getId());
            em.remove(managed);
        });

        Product deleted = emf.callInTransaction(em -> em.find(Product.class, product.getId()));

        assertThat(deleted).isNull();
    }

    @Test
    void statusIsStoredAsLowercaseTextInTheDatabase() {
        LocalDateTime now = LocalDateTime.now();
        Product product = new Product("Paracetamol 500mg", "Paracetamol 500mg", "Pain reliever", "Thuốc giảm đau",
                null, new BigDecimal("12.50"), "paracetamol.jpg", 100, Status.UNPUBLISHED, true, false, now, now);

        emf.runInTransaction(em -> {
            product.setCategory(persistCategory(em));
            em.persist(product);
        });

        String rawStatus = emf.callInTransaction(em ->
            (String) em.createNativeQuery("SELECT status FROM product WHERE id = ?1")
                .setParameter(1, product.getId())
                .getSingleResult());

        assertThat(rawStatus).isEqualTo("unpublished");
    }
}