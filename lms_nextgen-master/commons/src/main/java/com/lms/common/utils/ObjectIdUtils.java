package com.lms.common.utils;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class ObjectIdUtils {

    /**
     * Will not validate null ids
     *
     * @param ids
     * @return
     */
    public static boolean hasInvalidId(String... ids) {

        if (ids == null) {
            return false;
        }

        for (String id : ids) {
            if (null != id) {
                if (!ObjectId.isValid(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<ObjectId> toObjectIds(List<String> ids) {

        return toObjectIds(ids, false);
    }

    public static List<ObjectId> toObjectIds(List<String> ids, boolean removeInvalidIds) {

        if (null == ids) {
            return null;
        }
        List<ObjectId> objectIds = new ArrayList<ObjectId>();
        for (String id : ids) {
            if (removeInvalidIds && !ObjectId.isValid(id)) {
                continue;
            }
            objectIds.add(new ObjectId(id));
        }
        return objectIds;
    }


}
