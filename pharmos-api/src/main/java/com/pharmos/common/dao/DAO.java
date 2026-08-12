package com.pharmos.common.dao;

import java.util.List;

public interface DAO<T, K> {
    void create(T t);
    T read(K k);
    List<T> readAll();
    void update(T t);
    void delete(T t);
}
