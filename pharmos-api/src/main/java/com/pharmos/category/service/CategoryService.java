package com.pharmos.category.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pharmos.category.domain.Category;
import com.pharmos.common.dao.JPAFactory;

import jakarta.persistence.EntityManagerFactory;

@Service
public class CategoryService {

    private final EntityManagerFactory emf;

    public CategoryService() {
        this.emf = JPAFactory.getEmf("com.pharmos.common");
    }

    public void create(Category category) {
        emf.runInTransaction(em -> em.persist(category));
    }

    public Category read(Long id) {
        return emf.callInTransaction(em -> em.find(Category.class, id));
    }

    public List<Category> readAll() {
        return emf.callInTransaction(em -> em.createQuery("from Category", Category.class).getResultList());
    }

    public void update(Category category) {
        emf.runInTransaction(em -> em.merge(category));
    }

    public void delete(Category category) {
        emf.runInTransaction(em -> em.remove(em.contains(category) ? category : em.merge(category)));
    }
}
