package com.pharmos.common.dao;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAFactory {
    private static EntityManagerFactory emf;
    
    private JPAFactory() {}

    public static EntityManagerFactory getEmf(String pu) {
        if (emf == null)
            emf = Persistence.createEntityManagerFactory(pu);

        return emf;
    }

    public static void release() {
        if (emf != null && emf.isOpen())
            emf.close();
    }
}
