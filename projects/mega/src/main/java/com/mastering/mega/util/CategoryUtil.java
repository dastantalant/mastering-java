package com.mastering.mega.util;

import com.mastering.mega.number.model.Category;
import com.mastering.mega.number.model.CategoryItem;
import org.apache.commons.lang3.tuple.Pair;

import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class CategoryUtil {

    public Pair<Map<Integer, Category>, Map<Integer, CategoryItem>> init(List<Category> categories) {
        Map<Integer, Category> categoryMap = new HashMap<>();
        Map<Integer, CategoryItem> itemMap = new HashMap<>();

        for (Category category : categories) {
            processSubId(category);
            categoryMap.put(category.getId(), category);

            List<CategoryItem> items = category.getItems();

            if (items.isEmpty()) {
                itemMap.put(category.getId(), CategoryItem.from(category));
            }

            if (!items.isEmpty()) {
                items.forEach(item -> itemMap.put(item.id(), new CategoryItem(item.id(), item.price())));
            }
        }

        return Pair.of(Collections.unmodifiableMap(categoryMap),
                Collections.unmodifiableMap(itemMap));
    }

    private void processSubId(Category category) {
        List<CategoryItem> items = category.getItems();
        if (items != null) {
            int size = items.size();
            int[] subIds = new int[size + 1];
            subIds[0] = category.getId();
            for (int i = 1; i < size; i++) {
                subIds[i] = items.get(i).getId();
            }
            category.setSubId(subIds);
        }
    }
}
