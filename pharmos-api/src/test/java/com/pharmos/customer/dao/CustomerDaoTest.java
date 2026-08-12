package com.pharmos.customer.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.pharmos.common.dao.JPAFactory;
import com.pharmos.customer.domain.Customer;
import com.pharmos.customer.domain.PreferredLanguage;
import com.pharmos.customer.domain.Status;
import com.pharmos.testsupport.AbstractDaoIT;

import jakarta.persistence.PersistenceException;

public class CustomerDaoTest extends AbstractDaoIT {
    private final CustomerDao customerDao = new CustomerDao();

    @AfterEach
    void cleanDatabase() {
        JPAFactory.getEmf("com.pharmos.common").runInTransaction(em ->
            em.createNativeQuery("TRUNCATE TABLE address, customer RESTART IDENTITY CASCADE").executeUpdate());
    }

    @Test
    void createAndReadRoundTripsAllFields() {
        LocalDateTime emailVerifiedAt = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        LocalDateTime updatedAt = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        Customer customer = new Customer("a@example.com", "hashed-password", "Nguyen Van A", "0900000000",
                emailVerifiedAt, createdAt, updatedAt, PreferredLanguage.VI, Status.ACTIVE);
        customerDao.create(customer);

        Customer found = customerDao.read(customer.getId());

        assertThat(found.getEmail()).isEqualTo("a@example.com");
        assertThat(found.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(found.getFullName()).isEqualTo("Nguyen Van A");
        assertThat(found.getPhone()).isEqualTo("0900000000");
        assertThat(found.getEmailVerifiedAt()).isEqualTo(emailVerifiedAt);
        assertThat(found.getCreatedAt()).isEqualTo(createdAt);
        assertThat(found.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(found.getPreferredLanguage()).isEqualTo(PreferredLanguage.VI);
        assertThat(found.getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void updatePersistsChangesToExistingRow() {
        Customer customer = new Customer("b@example.com", "hashed-password", "Emily Johnson", null,
                null, LocalDateTime.now(), LocalDateTime.now(), PreferredLanguage.EN, Status.UNVERIFIED);
        customerDao.create(customer);

        customer.setFullName("Emily Johnson - updated");
        customer.setStatus(Status.ACTIVE);
        customerDao.update(customer);

        Customer found = customerDao.read(customer.getId());

        assertThat(found.getFullName()).isEqualTo("Emily Johnson - updated");
        assertThat(found.getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void deleteRemovesCustomer() {
        Customer customer = new Customer("c@example.com", "hashed-password", "Michael Davis", null,
                null, LocalDateTime.now(), LocalDateTime.now(), PreferredLanguage.EN, Status.ACTIVE);
        customerDao.create(customer);

        customerDao.delete(customer);

        assertThat(customerDao.read(customer.getId())).isNull();
    }

    @Test
    void creatingCustomerWithDuplicateEmailViolatesUniqueConstraint() {
        customerDao.create(new Customer("dup@example.com", "hashed-password", "Laura Chen", null,
                null, LocalDateTime.now(), LocalDateTime.now(), PreferredLanguage.EN, Status.ACTIVE));

        Customer duplicate = new Customer("dup@example.com", "hashed-password", "Sarah Wilson", null,
                null, LocalDateTime.now(), LocalDateTime.now(), PreferredLanguage.EN, Status.ACTIVE);

        assertThatThrownBy(() -> customerDao.create(duplicate))
            .isInstanceOf(PersistenceException.class);
    }
}
