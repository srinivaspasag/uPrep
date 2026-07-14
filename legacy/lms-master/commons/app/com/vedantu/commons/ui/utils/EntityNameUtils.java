package com.vedantu.commons.ui.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.vedantu.commons.enums.EntityType;

public class EntityNameUtils {

    private static Map<EntityType, String> entityTypeMap = new HashMap<EntityType, String>();

    static {
        entityTypeMap = new HashMap<EntityType, String>();
        entityTypeMap.put(EntityType.STATUSFEED, "post");

    }

    public static String getTypeName(EntityType entityType) {

        String name = entityTypeMap.get(entityType);

        if (StringUtils.isEmpty(name)) {
            name = entityType.name().toLowerCase();
        }
        return name;
    }

    public static String getTypeName(String entityType) {

        return getTypeName(EntityType.valueOfKey(entityType));
    }
}
