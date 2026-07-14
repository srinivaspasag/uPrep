package com.lms.common.utils;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class ListUtils {

    public static <T> T getFirst(List<T> inputList) {

        if (CollectionUtils.isNotEmpty(inputList)) {
            return inputList.get(0);
        }
        return null;
    }

    public static <T> T getLast(List<T> inputList) {

        if (CollectionUtils.isNotEmpty(inputList)) {
            return inputList.get(inputList.size() - 1);
        }
        return null;
    }
}
