package com.mastering.spring.specification.controller;

import com.mastering.spring.specification.dto.ListProductRequest;
import com.mastering.spring.specification.entity.Product;
import com.mastering.spring.specification.service.ProductService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @PostMapping("/filter")
    public ResponseEntity<List<Product>> getProducts(@RequestBody ListProductRequest request) {
        return ResponseEntity.ok(service.findAll(request));
    }
}
