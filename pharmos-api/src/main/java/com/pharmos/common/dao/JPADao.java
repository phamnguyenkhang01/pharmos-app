package com.pharmos.common.dao;

import java.lang.reflect.ParameterizedType;

import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public abstract class JPADao<T, K> implements DAO<T, K> {
    protected final EntityManagerFactory emf;
    private final Class<T> clazz;

    @SuppressWarnings("unchecked")
    public JPADao() {
        this.emf = JPAFactory.getEmf("com.pharmos.common");
        // get Entity class type
        this.clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public void create(T t) {
        emf.runInTransaction(em -> {
            em.persist(t);
        });
    }

    public T read(K k) {
        return emf.callInTransaction(em -> {
            return em.find(clazz, k);
        });
    }

    public List<T> readAll() {
        return emf.callInTransaction(em -> {
            return em.createQuery("from " + clazz.getName(), clazz).getResultList();
        });
    }

    @Override
    public void update(T t) {
        emf.runInTransaction(em -> {
            em.merge(t);
        });
    }

    @Override
    public void delete(T t){
        emf.runInTransaction(em-> {
            em.remove(em.contains(t) ? t : em.merge(t));
        });
    }
}
