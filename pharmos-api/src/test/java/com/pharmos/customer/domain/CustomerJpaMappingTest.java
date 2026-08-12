package com.pharmos.customer.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.pharmos.common.dao.JPAFactory;
import com.pharmos.testsupport.AbstractDaoIT;

public class CustomerJpaMappingTest extends AbstractDaoIT {
    private final jakarta.persistence.EntityManagerFactory emf = JPAFactory.getEmf("com.pharmos.common");

    @AfterEach
    void cleanDatabase() {
        emf.runInTransaction(em ->
            em.createNativeQuery("TRUNCATE TABLE customer RESTART IDENTITY CASCADE").executeUpdate());
    }

    @Test
    void customerPersistsAndLoadsThroughEntityManager() {
        LocalDateTime now = LocalDateTime.now();
        Customer customer = new Customer("a@example.com", "hashed-password", "Nguyen Van A",
                "0900000000", null, now, now, PreferredLanguage.VI, Status.ACTIVE);

        emf.runInTransaction(em -> em.persist(customer));

        Customer found = emf.callInTransaction(em -> em.find(Customer.class, customer.getId()));

        assertThat(found.getEmail()).isEqualTo("a@example.com");
        assertThat(found.getFullName()).isEqualTo("Nguyen Van A");
        assertThat(found.getPreferredLanguage()).isEqualTo(PreferredLanguage.VI);
        assertThat(found.getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void customerUpdatesThroughEntityManager() {
        LocalDateTime now = LocalDateTime.now();
        Customer customer = new Customer("b@example.com", "hashed-password", "Nguyen Van B",
                "0900000001", null, now, now, PreferredLanguage.VI, Status.UNVERIFIED);

        emf.runInTransaction(em -> em.persist(customer));

        emf.runInTransaction(em -> {
            Customer managed = em.find(Customer.class, customer.getId());
            managed.setStatus(Status.DISABLED);
            managed.setFullName("Nguyen Van B2");
        });

        Customer updated = emf.callInTransaction(em -> em.find(Customer.class, customer.getId()));

        assertThat(updated.getStatus()).isEqualTo(Status.DISABLED);
        assertThat(updated.getFullName()).isEqualTo("Nguyen Van B2");
    }

    @Test
    void customerDeletesThroughEntityManager() {
        LocalDateTime now = LocalDateTime.now();
        Customer customer = new Customer("c@example.com", "hashed-password", "Nguyen Van C",
                "0900000002", null, now, now, PreferredLanguage.VI, Status.ACTIVE);

        emf.runInTransaction(em -> em.persist(customer));

        emf.runInTransaction(em -> {
            Customer managed = em.find(Customer.class, customer.getId());
            em.remove(managed);
        });

        Customer deleted = emf.callInTransaction(em -> em.find(Customer.class, customer.getId()));

        assertThat(deleted).isNull();
    }

    @Test
    void statusIsStoredAsUppercaseTextInTheDatabase() {
        LocalDateTime now = LocalDateTime.now();
        Customer customer = new Customer("d@example.com", "hashed-password", "Nguyen Van D",
                "0900000003", null, now, now, PreferredLanguage.VI, Status.DISABLED);

        emf.runInTransaction(em -> em.persist(customer));

        String rawStatus = emf.callInTransaction(em ->
            (String) em.createNativeQuery("SELECT status FROM customer WHERE id = ?1")
                .setParameter(1, customer.getId())
                .getSingleResult());

        assertThat(rawStatus).isEqualTo("DISABLED");
    }
}
