package com.pharmos.product.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.pharmos.api.PharmosApiApplication;
import com.pharmos.category.domain.Category;
import com.pharmos.category.service.CategoryService;
import com.pharmos.common.dao.JPAFactory;
import com.pharmos.product.domain.Product;
import com.pharmos.product.domain.Status;
import com.pharmos.product.service.ProductService;
import com.pharmos.testsupport.AbstractDaoIT;

import org.springframework.test.context.DynamicPropertyRegistry;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(classes = PharmosApiApplication.class)
@AutoConfigureMockMvc
public class ProductControllerIT  extends AbstractDaoIT{
    
    @DynamicPropertySource
    static void pointSpringDatasourceAtTestcontainer 
    (DynamicPropertyRegistry registry)    {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username",
            POSTGRES::getUsername);
        registry.add("spring.datasource.password",
            POSTGRES::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();

    private Category category;

    @BeforeEach
    void createCategory() {
        category = new Category("Pain Relief", "Giảm đau", null, 1);
        categoryService.create(category);
    }

    @AfterEach
    void cleanDatabase() {
        JPAFactory.getEmf("com.pharmos.common").runInTransaction(em ->
                em.createNativeQuery("TRUNCATE TABLE product, category RESTART IDENTITY CASCADE").executeUpdate());
    }

    @Test
    void listOnlyReturnsPublishedProducts() throws Exception {
        productService.create(product("Paracetamol 500mg", Status.DRAFT));
        Product published = product("Ibuprofen 200mg", Status.PUBLISHED);
        productService.create(published);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(published.getId()))
                .andExpect(jsonPath("$[0].nameEn").value("Ibuprofen 200mg"));
    }

    @Test
    void detailReturnsRealNotFoundForDraftProduct() throws Exception {
        Product draft = product("Paracetamol 500mg", Status.DRAFT);
        productService.create(draft);

         mockMvc.perform(get("/api/products/{id}", draft.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void detailReturnsRealNotFoundForMissingProduct() throws Exception {
        mockMvc.perform(get("/api/products/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    void detailReturnsPublishedProductWithEmbeddedCategoryAndNoLazyFields() throws Exception {
        Product published = product("Ibuprofen 200mg", Status.PUBLISHED);
        productService.create(published);

        mockMvc.perform(get("/api/products/{id}", published.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nameEn").value("Ibuprofen 200mg"))
                .andExpect(jsonPath("$.category.id").value(category.getId()))
                .andExpect(jsonPath("$.category.nameEn").value("Pain Relief"))
                .andExpect(jsonPath("$.category.children").doesNotExist())
                .andExpect(jsonPath("$.category.parent").doesNotExist());
    }

    @Test
    void searchFiltersByKeywordCategoryPriceAndStock() throws Exception {
        Product match = product("Vitamin C 1000mg", Status.PUBLISHED);
        productService.create(match);
        productService.create(product("Ibuprofen 200mg", Status.PUBLISHED));

        mockMvc.perform(get("/api/products")
                        .param("keyword", "vitamin")
                        .param("categoryId", String.valueOf(category.getId()))
                        .param("minPrice", "1")
                        .param("maxPrice", "100")
                        .param("inStockOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(match.getId()));
    }

    private Product product(String name, Status status) {
        LocalDateTime now = LocalDateTime.now();
        return new Product(name, name, "description", "mô tả", category,
                new BigDecimal("9.99"), "image.jpg", 10, status, true, false, now, now);
    }
}
