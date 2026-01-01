package com.mastering.spring.specification.service.impl;

import com.mastering.spring.specification.dto.ListProductRequest;
import com.mastering.spring.specification.entity.Product;
import com.mastering.spring.specification.repository.ProductRepository;
import com.mastering.spring.specification.service.ProductService;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    @Override
    public List<Product> findAll(ListProductRequest request) {
        return repository.findAll(request.toSpecification());
    }
}
