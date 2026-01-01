package com.mastering.mega.util;

import lombok.experimental.UtilityClass;

import java.util.Arrays;

@UtilityClass
public class ArrayUtil {

    public static int[] prependIfAbsent(int mainId, int[] initialSubIds) {
        if (initialSubIds == null || initialSubIds.length == 0) {
            return new int[]{mainId};
        }

        boolean idAlreadyExists = Arrays.stream(initialSubIds)
                .anyMatch(currentId -> currentId == mainId);

        if (idAlreadyExists) {
            return initialSubIds;
        } else {
            int[] newSubId = new int[initialSubIds.length + 1];
            newSubId[0] = mainId;
            System.arraycopy(initialSubIds, 0, newSubId, 1, initialSubIds.length);
            return newSubId;
        }
    }

}
