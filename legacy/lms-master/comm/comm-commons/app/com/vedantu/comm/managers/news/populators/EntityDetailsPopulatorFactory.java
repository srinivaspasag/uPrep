package com.vedantu.comm.managers.news.populators;

import java.util.HashMap;
import java.util.Map;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.enums.EntityType;

public class EntityDetailsPopulatorFactory {

    private static final ALogger                LOGGER   = Logger.of(EntityDetailsPopulatorFactory.class);
    public static EntityDetailsPopulatorFactory INSTANCE = new EntityDetailsPopulatorFactory();

    private EntityDetailsPopulatorFactory() {

    }

    private Map<EntityType, IPopulator> entityDetailsPopulator = new HashMap<EntityType, IPopulator>();

    public synchronized boolean register(EntityType entityType, IPopulator basicPublisher) {

        if (!entityDetailsPopulator.containsKey(entityType)) {
            entityDetailsPopulator.put(entityType, basicPublisher);
        }
        return true;

    }

    public IPopulator get(EntityType entityType) {

        LOGGER.debug("Getting basic information : " + entityType.name());
        if (entityDetailsPopulator.containsKey(entityType)) {
            LOGGER.debug("Found DAO information : " + entityType.name());
            return entityDetailsPopulator.get(entityType);
        }

        LOGGER.debug("No populator found information : " + entityType);
        return null;

    }

}
