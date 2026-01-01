package com.mastering.mega.number.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    private int id;
    private String name;
    private String price;
    @Builder.Default
    private int[] subId = new int[0];

    @Builder.Default
    private List<CategoryItem> items = new ArrayList<>();
}