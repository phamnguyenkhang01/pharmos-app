package com.pharmos.product.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.pharmos.category.domain.Category;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "product")
public class Product implements Serializable {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name= "name_en", nullable = false)
    private String nameEn;

    @Column(name = "name_vi")
    private String nameVi;

    @Column(name="description_en")
    private String descriptionEn;

    @Column(name="description_vi")
    private String descriptionVi;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "image_ref")
    private String imageRef;

    @PositiveOrZero
    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Convert(converter = StatusConverter.class)
    private Status status;

    @Column(name = "is_medication", nullable = false)
    private boolean isMedication;

    @Column(name = "is_restricted", nullable = false)
    private boolean isRestricted;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Product() {
    }

    public Product(String nameEn, String nameVi, String descriptionEn, String descriptionVi, Category category,
            BigDecimal price, String imageRef, int stockQuantity, Status status, boolean isMedication, boolean isRestricted,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.nameEn = nameEn;
        this.nameVi = nameVi;
        this.descriptionEn = descriptionEn;
        this.descriptionVi = descriptionVi;
        this.category = category;
        this.price = price;
        this.imageRef = imageRef;
        this.stockQuantity = stockQuantity;
        this.status = status;
        this.isMedication = isMedication;
        this.isRestricted = isRestricted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameVi() {
        return nameVi;
    }

    public void setNameVi(String nameVi) {
        this.nameVi = nameVi;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public void setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }

    public String getDescriptionVi() {
        return descriptionVi;
    }

    public void setDescriptionVi(String descriptionVi) {
        this.descriptionVi = descriptionVi;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImageRef() {
        return imageRef;
    }

    public void setImageRef(String imageRef) {
        this.imageRef = imageRef;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isMedication() {
        return isMedication;
    }

    public void setMedication(boolean isMedication) {
        this.isMedication = isMedication;
    }

    public boolean isRestricted() {
        return isRestricted;
    }

    public void setRestricted(boolean isRestricted) {
        this.isRestricted = isRestricted;
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

    @Override
    public String toString() {
        return "Product [id=" + id + ", nameEn=" + nameEn + ", nameVi=" + nameVi + ", descriptionEn=" + descriptionEn
                + ", descriptionVi=" + descriptionVi + ", categoryId=" + (category != null ? category.getId() : null) + ", price=" + price + ", imageRef="
                + imageRef + ", stockQuantity=" + stockQuantity + ", status=" + status + ", isMedication="
                + isMedication + ", isRestricted=" + isRestricted + ", createdAt=" + createdAt + ", updatedAt="
                + updatedAt + "]";
    }
}
