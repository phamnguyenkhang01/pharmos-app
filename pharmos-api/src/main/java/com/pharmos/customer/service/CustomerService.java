package com.pharmos.customer.service;

import org.mindrot.jbcrypt.BCrypt;

import com.pharmos.common.dao.JPAFactory;
import com.pharmos.customer.domain.Customer;

import jakarta.persistence.EntityManagerFactory;

public class CustomerService {

    private final EntityManagerFactory emf;

    public CustomerService() {
        this.emf = JPAFactory.getEmf("com.pharmos.common");
    }

    public void create(Customer customer) {
        String hashedPassword = BCrypt.hashpw((customer.getPasswordHash()), BCrypt.gensalt());
        customer.setPasswordHash(hashedPassword);
        emf.runInTransaction(em -> em.persist(customer));
    }

    public Customer read(Long id) {
        return emf.callInTransaction(em -> em.find(Customer.class, id));
    }

    public void update(Customer customer) {
        emf.runInTransaction(em -> em.merge(customer));
    }

    public void delete(Customer customer) {
        emf.runInTransaction(em -> em.remove(em.contains(customer) ? customer : em.merge(customer)));
    }

    public boolean checkPassword(String enteredPassword, Long id)
    {
        Customer customer = read(id);
        if (customer == null) {
            return false;
        }
        return BCrypt.checkpw(enteredPassword, customer.getPasswordHash());
    }
}
