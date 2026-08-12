package com.pharmos.product.web;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.HttpStatus;
import com.pharmos.product.domain.Product;
import com.pharmos.product.domain.Status;
import com.pharmos.product.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "false") boolean inStockOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return productService.search(keyword, categoryId, minPrice, maxPrice, inStockOnly, page, size);
    }

    @GetMapping("/{id}")
    public Product detail(@PathVariable Long id) {
        Product product = productService.read(id);
        if (product == null || product.getStatus() != Status.PUBLISHED){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return product;
    }
}