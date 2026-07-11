package com.vedantu.content.factory;

import java.util.HashMap;
import java.util.Map;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.content.search.details.AbstractSearchDetail;

public class EntityTypeSearchDetailsFactory {

    private static final ALogger                 LOGGER                         = Logger.of(EntityTypeSearchDetailsFactory.class);
    public static EntityTypeSearchDetailsFactory INSTANCE                       = new EntityTypeSearchDetailsFactory();

    private Map<String, AbstractSearchDetail>    entityTypeSearchDetailsFactory = new HashMap<String, AbstractSearchDetail>();

    private EntityTypeSearchDetailsFactory() {

    }

    public boolean register(EntityType entityType, AbstractSearchDetail basicDAO) {

        entityTypeSearchDetailsFactory.put(entityType.name(), basicDAO);
        return true;

    }

    public AbstractSearchDetail get(EntityType entityType) {

        LOGGER.debug("Getting basic information : " + entityType.name());
        if (entityTypeSearchDetailsFactory.containsKey(entityType.name())) {
            LOGGER.debug("Found DAO information : " + entityType.name());
            return entityTypeSearchDetailsFactory.get(entityType.name());
        }

        LOGGER.debug("No DAO found information : " + entityType.name());
        return null;

    }

}