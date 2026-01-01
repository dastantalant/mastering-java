package com.mastering.mega.config;

import com.mastering.mega.number.model.Category;
import com.mastering.mega.number.model.CategoryItem;
import com.mastering.mega.util.CategoryUtil;
import org.apache.commons.lang3.tuple.Pair;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "category")
public class CategoryProperties {

    private List<Category> list = new ArrayList<>();

    private Map<Integer, Category> categoriesMap;
    private Map<Integer, CategoryItem> nodesMap;
    private int[] allCategories;

    public int[] getAllCategories() {
        if (allCategories == null) {
            Set<Integer> set = nodesMap.keySet();
            allCategories = new int[set.size()];
            Iterator<Integer> iterator = set.iterator();
            for (int i = 0; i < set.size(); i++) {
                allCategories[i] = iterator.next();
            }
        }
        return allCategories;
    }

    @PostConstruct
    public void init() {
        Pair<Map<Integer, Category>, Map<Integer, CategoryItem>> pair = CategoryUtil.init(list);

        categoriesMap = pair.getLeft();
        nodesMap = pair.getRight();
    }
}