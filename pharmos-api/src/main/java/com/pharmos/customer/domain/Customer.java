package com.pharmos.customer.domain;

import com.pharmos.address.domain.Address;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;


@Entity
@Table(name = "customer")
public class Customer implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy="customer", cascade=CascadeType.ALL, orphanRemoval= true)
    private List<Address> addresses = new ArrayList<>();

    @NotNull
    @Column(name="password_hash", nullable= false)
    private String passwordHash;

    @NotNull
    @Column(name="full_name", nullable= false)
    private String fullName;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name="preferred_language")
    private PreferredLanguage preferredLanguage;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name="email_verified_at")
    private LocalDateTime emailVerifiedAt; 

    @NotNull
    @Column(name="created_at", nullable= false)
    private LocalDateTime createdAt;

    @NotNull
    @Column(name="updated_at", nullable= false)
    private LocalDateTime updatedAt;

    public Customer() {}

    public Customer(String email, String passwordHash, String fullName, String phone, LocalDateTime emailVerifiedAt,
            LocalDateTime createdAt, LocalDateTime updatedAt, PreferredLanguage preferredLanguage, Status status) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.phone = phone;
        this.emailVerifiedAt = emailVerifiedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.preferredLanguage = preferredLanguage;
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(LocalDateTime emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }


    public Long getId() {
        return id;
    }


    public PreferredLanguage getPreferredLanguage() {
    return preferredLanguage;
    }

    public void setPreferredLanguage(PreferredLanguage preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }   

    @Override
    public String toString() {
        return "Customer [id=" + id + ", email=" + email + ", fullName=" + fullName + ", phone=" + phone
                + ", emailVerifiedAt=" + emailVerifiedAt + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
                + ", preferredLanguage=" + preferredLanguage + ", status=" + status + "]";
    }
    // Todo: Update toString with addresses?

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses.clear();
        if (addresses != null) {
            this.addresses.addAll(addresses);
        }
    }
}