package com.pharmos.testsupport;

import java.lang.reflect.Field;
import java.util.Map;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.pharmos.common.dao.JPAFactory;

/**
 * Base class for DAO tests that need real Postgres/Hibernate behavior instead of H2.
 *
 * The DAO layer ({@link com.pharmos.common.dao.JPADao}) does not go through Spring's
 * datasource - it pulls a process-wide singleton {@link EntityManagerFactory} out of
 * {@link JPAFactory}, built from persistence.xml's hardcoded JDBC URL. There is no supported
 * hook to redirect that singleton at a test container, so this base class starts one shared
 * Postgres container per JVM, migrates it with Flyway, and reflectively swaps JPAFactory's
 * static "emf" field before any DAO can initialize it from persistence.xml. Because that swap
 * only wins if it happens before the first JPADao subclass is constructed in this JVM, Surefire
 * is configured (see pom.xml) to fork a fresh JVM per test class so no other test can race this
 * one to initialize the singleton first.
 */

@Testcontainers
public abstract class AbstractDaoIT {
        protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("pharmos")
        .withUsername("pharmos")
        .withPassword("pharmos");
    
    @BeforeAll
    static void startContainerAndPrimeJpaFactory() throws Exception {
        POSTGRES.start();

        Flyway.configure()
            .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
            .locations("classpath:db/migration")
            .load()
            .migrate();

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.pharmos.common", Map.of(
            "jakarta.persistence.jdbc.url", POSTGRES.getJdbcUrl(),
                "jakarta.persistence.jdbc.user", POSTGRES.getUsername(),
                "jakarta.persistence.jdbc.password", POSTGRES.getPassword()));

        Field emfField = JPAFactory.class.getDeclaredField("emf");
        emfField.setAccessible(true);
        emfField.set(null, emf);
    }
}
