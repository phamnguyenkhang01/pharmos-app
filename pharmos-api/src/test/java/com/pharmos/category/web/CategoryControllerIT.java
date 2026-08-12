package com.pharmos.category.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.pharmos.api.PharmosApiApplication;
import com.pharmos.category.domain.Category;
import com.pharmos.category.service.CategoryService;
import com.pharmos.common.dao.JPAFactory;
import com.pharmos.testsupport.AbstractDaoIT;

@SpringBootTest(classes = PharmosApiApplication.class)
@AutoConfigureMockMvc
class CategoryControllerIT extends AbstractDaoIT {

    @DynamicPropertySource
    static void pointSpringDatasourceAtTestcontainer(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    private final CategoryService categoryService = new CategoryService();

    @AfterEach
    void cleanDatabase() {
        JPAFactory.getEmf("com.pharmos.common").runInTransaction(em ->
                em.createNativeQuery("TRUNCATE TABLE category RESTART IDENTITY CASCADE").executeUpdate());
    }

    @Test
    void listReturnsCategoriesWithoutLazyParentOrChildrenFields() throws Exception {
        Category parent = new Category("Medication", "Thuốc", null, 1);
        categoryService.create(parent);
        Category child = new Category("Pain Relief", "Giảm đau", parent, 2);
        categoryService.create(child);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.nameEn == 'Pain Relief')].children").doesNotExist())
                .andExpect(jsonPath("$[?(@.nameEn == 'Pain Relief')].parent").doesNotExist());
    }
}