package com.vedantu.commons.utils;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

public class ListUtils {

    public static  <T> T  getFirst(List<T> inputList) {

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
