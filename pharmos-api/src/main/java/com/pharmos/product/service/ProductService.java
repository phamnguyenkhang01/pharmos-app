package com.pharmos.product.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pharmos.common.dao.JPAFactory;
import com.pharmos.product.domain.Product;
import com.pharmos.product.domain.Status;

import jakarta.persistence.EntityManagerFactory;

@Service
public class ProductService {

    private static final String SEARCH_QUERY = """
            select p from Product p
            where p.status = :status
            and (:categoryId is null or p.category.id = :categoryId)
            and (:likePattern is null or lower(p.nameEn) like :likePattern or lower(p.nameVi) like :likePattern)
            and (:minPrice is null or p.price >= :minPrice)
            and (:maxPrice is null or p.price <= :maxPrice)
            and (:inStockOnly = false or p.stockQuantity > 0)
            order by p.id
            """;

    private final EntityManagerFactory emf;

    public ProductService() {
        this.emf = JPAFactory.getEmf("com.pharmos.common");
    }

    public void create(Product product) {
        emf.runInTransaction(em -> em.persist(product));
    }

    public Product read(Long id) {
        return emf.callInTransaction(em -> em.find(Product.class, id));
    }

    public List<Product> readAll() {
        return emf.callInTransaction(em -> em.createQuery("from Product", Product.class).getResultList());
    }

    public List<Product> search(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
        boolean inStockOnly, int page, int size) {
            if (page < 0 || size <= 0) {
                throw new IllegalArgumentException("page must be >= 0 and size must be > 0");
            }
            String likePattern = keyword == null || keyword.isBlank()? null : "%" + keyword.trim().toLowerCase() + "%";

            return emf.callInTransaction(em -> em.createQuery(SEARCH_QUERY, Product.class)
                    .setParameter("status", Status.PUBLISHED)
                    .setParameter("categoryId", categoryId)
                    .setParameter("likePattern", likePattern)
                    .setParameter("minPrice", minPrice)
                    .setParameter("maxPrice", maxPrice)
                    .setParameter("inStockOnly", inStockOnly)
                    .setFirstResult(page * size)
                    .setMaxResults(size)
                    .getResultList());
        }

    public void update(Product product) {
        emf.runInTransaction(em -> em.merge(product));
    }

    public void delete(Product product) {
        emf.runInTransaction(em -> em.remove(em.contains(product) ? product : em.merge(product)));
    }
}
