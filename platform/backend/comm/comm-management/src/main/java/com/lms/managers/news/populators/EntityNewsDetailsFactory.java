package com.lms.managers.news.populators;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class EntityNewsDetailsFactory {
    private static final Logger logger = LoggerFactory.getLogger(EntityNewsDetailsFactory.class);

    public static EntityNewsDetailsFactory INSTANCE = new EntityNewsDetailsFactory();
    private final Map<EntityType, Class<? extends SrcEntity>> entityDetailsMap = new HashMap<EntityType, Class<? extends SrcEntity>>();

    private EntityNewsDetailsFactory() {

    }

    public boolean register(EntityType entityType,
                            Class<? extends SrcEntity> detailsClazz) {
        entityDetailsMap.put(entityType, detailsClazz);
        return true;

    }

    public SrcEntity getInstance(EntityType entityType) {
        logger.debug("Getting basic information : " + entityType.name());
        SrcEntity newDetails = null;
        try {
            if (entityDetailsMap.containsKey(entityType)) {
                logger.debug("Found EntityDetails information : " + entityType.name());
                Class<? extends SrcEntity> entityNewsDetailsClazz = entityDetailsMap
                        .get(entityType);

                newDetails = entityNewsDetailsClazz.newInstance();
            }
        } catch (InstantiationException e) {
            logger.error("can not instantiate", e);
        } catch (IllegalAccessException e) {
            logger.error("Illegal not instantiate", e);
        }
        if (newDetails == null)
            logger.debug("No entity details class found  information : "
                    + entityType);
        return newDetails;

    }
}
