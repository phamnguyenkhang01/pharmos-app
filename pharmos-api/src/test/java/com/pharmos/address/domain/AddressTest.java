package com.pharmos.address.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pharmos.customer.domain.Customer;
import com.pharmos.customer.domain.PreferredLanguage;
import com.pharmos.customer.domain.Status;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

public class AddressTest {
    private static Validator validator;

    private Customer customer;
    private LocalDateTime createdAt;
    private Address address;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @BeforeEach
    void setUp() {
        customer = new Customer("john.smith@example.com", "hashed-password", "John Smith", "512-555-0148",
                null, LocalDateTime.now(), LocalDateTime.now(), PreferredLanguage.EN, Status.ACTIVE);
        createdAt = LocalDateTime.now();
        address = new Address(customer, "John Smith", "123 Congress Ave", "Apt 4", "Austin",
                "TX", "78701", "512-555-0100", true, createdAt);
    }

    @Test
    void constructorSetsAllFields() {
        assertThat(address.getCustomer()).isEqualTo(customer);
        assertThat(address.getRecipientName()).isEqualTo("John Smith");
        assertThat(address.getStreetLine1()).isEqualTo("123 Congress Ave");
        assertThat(address.getStreetLine2()).isEqualTo("Apt 4");
        assertThat(address.getCity()).isEqualTo("Austin");
        assertThat(address.getState()).isEqualTo("TX");
        assertThat(address.getZip()).isEqualTo("78701");
        assertThat(address.getPhone()).isEqualTo("512-555-0100");
        assertThat(address.getIsDefault()).isTrue();
        assertThat(address.getCreatedAt()).isEqualTo(createdAt);
        assertThat(address.getId()).isNull();
    }

    @Test
    void settersUpdateFields() {
        Customer newCustomer = new Customer("emily.johnson@example.com", "hashed-password", "Emily Johnson",
                "415-555-0173", null, LocalDateTime.now(), LocalDateTime.now(), PreferredLanguage.EN, Status.ACTIVE);
        LocalDateTime newCreatedAt = LocalDateTime.now().minusDays(1);

        address.setCustomer(newCustomer);
        address.setRecipientName("Emily Johnson");
        address.setStreetLine1("45 Market St");
        address.setStreetLine2(null);
        address.setCity("San Francisco");
        address.setState("CA");
        address.setZip("94103");
        address.setPhone(null);
        address.setIsDefault(false);
        address.setCreatedAt(newCreatedAt);

        assertThat(address.getCustomer()).isEqualTo(newCustomer);
        assertThat(address.getRecipientName()).isEqualTo("Emily Johnson");
        assertThat(address.getStreetLine1()).isEqualTo("45 Market St");
        assertThat(address.getStreetLine2()).isNull();
        assertThat(address.getCity()).isEqualTo("San Francisco");
        assertThat(address.getState()).isEqualTo("CA");
        assertThat(address.getZip()).isEqualTo("94103");
        assertThat(address.getPhone()).isNull();
        assertThat(address.getIsDefault()).isFalse();
        assertThat(address.getCreatedAt()).isEqualTo(newCreatedAt);
    }

    @Test
    void validAddressHasNoViolations() {
        Set<ConstraintViolation<Address>> violations = validator.validate(address);

        assertThat(violations).isEmpty();
    }

    @Test
    void optionalStreetLine2AndPhoneCanBeNullWithoutViolations() {
        address.setStreetLine2(null);
        address.setPhone(null);

        assertThat(validator.validate(address)).isEmpty();
    }

    @Test
    void validationFailsWhenRecipientNameIsNull() {
        address.setRecipientName(null);

        assertThat(propertyPaths(validator.validate(address))).contains("recipientName");
    }

    @Test
    void validationFailsWhenStreetLine1IsNull() {
        address.setStreetLine1(null);

        assertThat(propertyPaths(validator.validate(address))).contains("streetLine1");
    }

    @Test
    void validationFailsWhenCityIsNull() {
        address.setCity(null);

        assertThat(propertyPaths(validator.validate(address))).contains("city");
    }

    @Test
    void validationFailsWhenStateIsNull() {
        address.setState(null);

        assertThat(propertyPaths(validator.validate(address))).contains("state");
    }

    @Test
    void validationFailsWhenZipIsNull() {
        address.setZip(null);

        assertThat(propertyPaths(validator.validate(address))).contains("zip");
    }

    @Test
    void validationFailsWhenIsDefaultIsNull() {
        address.setIsDefault(null);

        assertThat(propertyPaths(validator.validate(address))).contains("isDefault");
    }

    @Test
    void validationFailsWhenCreatedAtIsNull() {
        address.setCreatedAt(null);

        assertThat(propertyPaths(validator.validate(address))).contains("createdAt");
    }

    private static List<String> propertyPaths(Set<ConstraintViolation<Address>> violations) {
        return violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();
    }
}
