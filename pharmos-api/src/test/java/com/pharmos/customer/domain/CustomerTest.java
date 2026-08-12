package com.pharmos.customer.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pharmos.address.domain.Address;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

public class CustomerTest {
    private static Validator validator;

    private LocalDateTime emailVerifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Customer customer;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @BeforeEach
    void setUp() {
        emailVerifiedAt = LocalDateTime.now().minusDays(1);
        createdAt = LocalDateTime.now().minusDays(2);
        updatedAt = LocalDateTime.now();
        customer = new Customer("john.smith@example.com", "hashed-password", "John Smith", "512-555-0148",
                emailVerifiedAt, createdAt, updatedAt, PreferredLanguage.EN, Status.ACTIVE);
    }

    @Test
    void constructorSetsAllFields() {
        assertThat(customer.getEmail()).isEqualTo("john.smith@example.com");
        assertThat(customer.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(customer.getFullName()).isEqualTo("John Smith");
        assertThat(customer.getPhone()).isEqualTo("512-555-0148");
        assertThat(customer.getEmailVerifiedAt()).isEqualTo(emailVerifiedAt);
        assertThat(customer.getCreatedAt()).isEqualTo(createdAt);
        assertThat(customer.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(customer.getPreferredLanguage()).isEqualTo(PreferredLanguage.EN);
        assertThat(customer.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(customer.getId()).isNull();
    }

    @Test
    void noArgConstructorLeavesAddressesEmptyNotNull() {
        Customer blank = new Customer();

        assertThat(blank.getAddresses()).isNotNull();
        assertThat(blank.getAddresses()).isEmpty();
    }

    @Test
    void settersUpdateFields() {
        LocalDateTime newEmailVerifiedAt = LocalDateTime.now();
        LocalDateTime newCreatedAt = LocalDateTime.now().minusDays(5);
        LocalDateTime newUpdatedAt = LocalDateTime.now();

        customer.setEmail("emily.johnson@example.com");
        customer.setPasswordHash("new-hashed-password");
        customer.setFullName("Emily Johnson");
        customer.setPhone("415-555-0173");
        customer.setEmailVerifiedAt(newEmailVerifiedAt);
        customer.setCreatedAt(newCreatedAt);
        customer.setUpdatedAt(newUpdatedAt);
        customer.setPreferredLanguage(PreferredLanguage.EN);
        customer.setStatus(Status.DISABLED);

        assertThat(customer.getEmail()).isEqualTo("emily.johnson@example.com");
        assertThat(customer.getPasswordHash()).isEqualTo("new-hashed-password");
        assertThat(customer.getFullName()).isEqualTo("Emily Johnson");
        assertThat(customer.getPhone()).isEqualTo("415-555-0173");
        assertThat(customer.getEmailVerifiedAt()).isEqualTo(newEmailVerifiedAt);
        assertThat(customer.getCreatedAt()).isEqualTo(newCreatedAt);
        assertThat(customer.getUpdatedAt()).isEqualTo(newUpdatedAt);
        assertThat(customer.getPreferredLanguage()).isEqualTo(PreferredLanguage.EN);
        assertThat(customer.getStatus()).isEqualTo(Status.DISABLED);
    }

    @Test
    void setAddressesReplacesContentsInPlace() {
        Address first = new Address(customer, "John Smith", "123 Congress Ave", null, "Austin",
                "TX", "78701", null, true, LocalDateTime.now());
        Address second = new Address(customer, "Emily Johnson", "45 Market St", null, "San Francisco",
                "CA", "94103", null, false, LocalDateTime.now());

        customer.setAddresses(List.of(first));
        assertThat(customer.getAddresses()).containsExactly(first);

        customer.setAddresses(List.of(second));
        assertThat(customer.getAddresses()).containsExactly(second);
    }

    @Test
    void validCustomerHasNoViolations() {
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);

        assertThat(violations).isEmpty();
    }

    @Test
    void validationFailsWhereEmailIsNull() {
        customer.setEmail(null);

        assertThat(propertyPaths(validator.validate(customer))).contains("email");
    }

    @Test
    void validationFailsWhenPasswordHashIsNull() {
        customer.setPasswordHash(null);

        assertThat(propertyPaths(validator.validate(customer))).contains("passwordHash");
    }

    @Test
    void validationFailsWhenFullNameIsNull() {
        customer.setFullName(null);

        assertThat(propertyPaths(validator.validate(customer))).contains("fullName");
    }

    @Test
    void validationFailsWhenCreatedAtIsNull() {
        customer.setCreatedAt(null);

        assertThat(propertyPaths(validator.validate(customer))).contains("createdAt");
    }

    @Test
    void validationFailsWhenUpdatedAtIsNull() {
        customer.setUpdatedAt(null);

        assertThat(propertyPaths(validator.validate(customer))).contains("updatedAt");
    }

    private static List<String> propertyPaths(Set<ConstraintViolation<Customer>> violations) {
        return violations.stream()
        .map(v -> v.getPropertyPath().toString())
        .toList();
    }
}
