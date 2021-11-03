package com.mob.sms3.utils;

import java.util.List;

public class CollectionUtils {
    public static <T> T get(List<T> list, int index) {
        if (list == null || list.isEmpty()) return null;
        if (list.size() > index) {
            return list.get(index);
        }
        return null;
    }
}
