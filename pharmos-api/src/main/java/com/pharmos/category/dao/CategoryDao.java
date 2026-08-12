package com.pharmos.category.dao;

import org.springframework.stereotype.Repository;

import com.pharmos.category.domain.Category;
import com.pharmos.common.dao.JPADao;

@Repository
public class CategoryDao extends JPADao<Category, Long> {

}
