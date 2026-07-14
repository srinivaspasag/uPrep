package com.vedantu.comm.utils.news;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;

public class NewsUtils {

    private final static ALogger LOGGER = Logger.of(NewsUtils.class);

    public static Map<String, SrcEntity> getSrcEntityIds(Set<SrcEntity> newsEntities) {

        Map<String, SrcEntity> entityIds = new HashMap<String, SrcEntity>();
        for (SrcEntity entity : newsEntities) {
            String id = entity.id;
            // if (entity instanceof CommentSrcEntity) {
            // CommentSrcEntity c = (CommentSrcEntity) entity;
            // id = c.commId;
            // }
            // if (entity instanceof PlaylistSrcEntityDetails) {
            // PlaylistSrcEntityDetails c = (PlaylistSrcEntityDetails) entity;
            // id = c.id;
            // }
            LOGGER.debug(" Entity Details from collected news Entities:" + entity.id + " type "
                    + entity.type);
            entityIds.put(id, entity);
        }
        return entityIds;
    }

    // disambiguation in case of lots of events happen at the same instance
    private static final int MAX_RAND = 99999;

    public static String getRowId(SrcEntity newsEntity, long time) {

        StringBuilder s = new StringBuilder();
        s.append(newsEntity.id);
        s.append("_").append(newsEntity.type.getQualifierChar());
        s.append("_").append(Long.MAX_VALUE - time);
        s.append("_").append(new Random().nextInt(MAX_RAND));
        return s.toString();
    }

    @Deprecated
    public static String getRowId(SrcEntity newsEntity) {

        StringBuilder s = new StringBuilder(newsEntity.id);
        s.append("_").append(newsEntity.type.getQualifierChar());
        return s.toString();
    }

    private static final int ROW_ID_COMPONENT_COUNT = 3;

    public static boolean isValidRowId(String rowId) {

        String[] tokens = StringUtils.split(rowId, "_");
        if (tokens.length < ROW_ID_COMPONENT_COUNT) {
            Logger.error("invalid no. of id-components (" + tokens.length + ") in rowId : " + rowId);
            return false;
        }

        final String entityId = tokens[0];
        final String entityType = tokens[1];
        final String timeValue = tokens[2];

        if (StringUtils.isEmpty(entityId)) {
            Logger.error("invalid user in rowId : " + rowId);
            return false;
        }

        if (!EntityType.isValidQualifierChar(entityType)) {
            Logger.error("invalid entityType (" + entityType + ") in rowId : " + rowId);
            return false;
        }
        if (StringUtils.isEmpty(timeValue) || !NumberUtils.isNumber(timeValue)) {
            Logger.error("invalid timeValue (" + timeValue + ") in rowId : " + rowId);
            return false;
        }

        return true;
    }
}
