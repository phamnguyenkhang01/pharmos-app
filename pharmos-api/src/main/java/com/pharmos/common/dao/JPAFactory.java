package com.pharmos.common.dao;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAFactory {
    private static EntityManagerFactory emf;

    private JPAFactory() {}

    public static EntityManagerFactory getEmf(String pu) {
        if (emf == null)
            emf = Persistence.createEntityManagerFactory(pu, envOverrides());

        return emf;
    }

    /**
     * persistence.xml hardcodes localhost JDBC settings for local dev; these overrides let
     * deployed environments (e.g. SPRING_DATASOURCE_URL) redirect this factory the same way
     * application.properties redirects Spring's own datasource.
     */
    private static Map<String, String> envOverrides() {
        Map<String, String> overrides = new HashMap<>();
        putIfPresent(overrides, "jakarta.persistence.jdbc.url", System.getenv("SPRING_DATASOURCE_URL"));
        putIfPresent(overrides, "jakarta.persistence.jdbc.user", System.getenv("SPRING_DATASOURCE_USERNAME"));
        putIfPresent(overrides, "jakarta.persistence.jdbc.password", System.getenv("SPRING_DATASOURCE_PASSWORD"));
        return overrides;
    }

    private static void putIfPresent(Map<String, String> map, String key, String value) {
        if (value != null)
            map.put(key, value);
    }

    public static void release() {
        if (emf != null && emf.isOpen())
            emf.close();
    }
}
