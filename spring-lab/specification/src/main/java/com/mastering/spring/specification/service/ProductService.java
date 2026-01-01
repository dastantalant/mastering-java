package com.mastering.spring.specification.service;

import com.mastering.spring.specification.dto.ListProductRequest;
import com.mastering.spring.specification.entity.Product;

import java.util.List;

public interface ProductService {

    List<Product> findAll(ListProductRequest request);
}
