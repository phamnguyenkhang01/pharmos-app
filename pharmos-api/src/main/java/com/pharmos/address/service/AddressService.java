package com.pharmos.address.service;

import org.springframework.stereotype.Service;

import com.pharmos.address.domain.Address;
import com.pharmos.common.dao.JPAFactory;

import jakarta.persistence.EntityManagerFactory;

@Service
public class AddressService {

    private final EntityManagerFactory emf;

    public AddressService() {
        this.emf = JPAFactory.getEmf("com.pharmos.common");
    }

    public void create(Address address) {
        emf.runInTransaction(em -> em.persist(address));
    }

    public Address read(Long id) {
        return emf.callInTransaction(em -> em.find(Address.class, id));
    }

    public void update(Address address) {
        emf.runInTransaction(em -> em.merge(address));
    }

    public void delete(Address address) {
        emf.runInTransaction(em -> em.remove(em.contains(address) ? address : em.merge(address)));
    }
}
