package com.pharmos.address.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.pharmos.common.dao.JPAFactory;
import com.pharmos.customer.domain.Customer;
import com.pharmos.customer.domain.PreferredLanguage;
import com.pharmos.customer.domain.Status;
import com.pharmos.testsupport.AbstractDaoIT;

public class AddressJpaMappingTest extends AbstractDaoIT {
    private final jakarta.persistence.EntityManagerFactory emf = JPAFactory.getEmf("com.pharmos.common");

    @AfterEach
    void cleanDatabase() {
        emf.runInTransaction(em ->
            em.createNativeQuery("TRUNCATE TABLE address, customer RESTART IDENTITY CASCADE").executeUpdate());
    }

    @Test
    void addressPersistsAndLoadsThroughEntityManager() {
        
        Customer customer = new Customer("a@example.com", "hashed-password", "Nguyen Van A",
                "0900000000", null, LocalDateTime.now(), LocalDateTime.now(),
                PreferredLanguage.VI, Status.ACTIVE);

        Address address = new Address(customer, "John Smith", "123 Congress Ave", "Apt 4",
                "Austin", "TX", "78701", "5125550100", true, LocalDateTime.now());

        emf.runInTransaction(em -> {
            em.persist(customer);
            em.persist(address);
        });

        Address found = emf.callInTransaction(em -> em.find(Address.class, address.getId()));

        assertThat(found.getRecipientName()).isEqualTo("John Smith");
        assertThat(found.getCustomer().getId()).isEqualTo(customer.getId());
    }

    @Test
    void addressUpdatesThroughEntityManager() {
        Customer customer = new Customer("b@example.com", "hashed-password", "Nguyen Van B",
                "0900000001", null, LocalDateTime.now(), LocalDateTime.now(),
                PreferredLanguage.VI, Status.ACTIVE);

        Address address = new Address(customer, "John Smith", "123 Congress Ave", "Apt 4",
                "Austin", "TX", "78701", "5125550100", true, LocalDateTime.now());

        emf.runInTransaction(em -> {
            em.persist(customer);
            em.persist(address);
        });

        emf.runInTransaction(em -> {
            Address managed = em.find(Address.class, address.getId());
            managed.setRecipientName("Jane Doe");
            managed.setStreetLine1("456 Main St");
            managed.setCity("Dallas");
            managed.setIsDefault(false);
        });

        Address updated = emf.callInTransaction(em -> em.find(Address.class, address.getId()));

        assertThat(updated.getRecipientName()).isEqualTo("Jane Doe");
        assertThat(updated.getStreetLine1()).isEqualTo("456 Main St");
        assertThat(updated.getCity()).isEqualTo("Dallas");
        assertThat(updated.getIsDefault()).isFalse();
    }

    @Test
    void addressDeletesThroughEntityManager() {
        Customer customer = new Customer("c@example.com", "hashed-password", "Nguyen Van C",
                "0900000002", null, LocalDateTime.now(), LocalDateTime.now(),
                PreferredLanguage.VI, Status.ACTIVE);

        Address address = new Address(customer, "John Smith", "123 Congress Ave", "Apt 4",
                "Austin", "TX", "78701", "5125550100", true, LocalDateTime.now());
        
        emf.runInTransaction(em -> {
            em.persist(customer);
            em.persist(address);
        });

        emf.runInTransaction(em -> {
            Address managed = em.find(Address.class, address.getId());
            em.remove(managed);
        });

        Address deleted = emf.callInTransaction(em -> em.find(Address.class, address.getId()));

        assertThat(deleted).isNull();
    }
}