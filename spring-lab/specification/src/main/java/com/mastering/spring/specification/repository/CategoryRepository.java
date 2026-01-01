package com.mastering.spring.specification.repository;

import com.mastering.spring.specification.entity.Category;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
