package com.vedantu.commons.entity.factory;

import java.util.HashMap;
import java.util.Map;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.content.interfaces.IContentManager;
import com.vedantu.commons.enums.EntityType;

public class EntityTypeContentManagerFactory {

    private static final ALogger                  LOGGER                   = Logger.of(EntityTypeContentManagerFactory.class);
    public static EntityTypeContentManagerFactory INSTANCE                 = new EntityTypeContentManagerFactory();

    private Map<String, Class<?>>                 entityTypeManagerFactory = new HashMap<String, Class<?>>();

    private EntityTypeContentManagerFactory() {

    }

    public boolean register(EntityType entityType, Class<?> basicManager) {

        if (basicManager.isAssignableFrom(IContentManager.class)) {
            LOGGER.error("is not instance of IContentManager");
            return false;
        }
        entityTypeManagerFactory.put(entityType.name(), basicManager);
        return true;

    }

    public IContentManager get(EntityType entityType) throws VedantuException {

        Class<?> manager = entityTypeManagerFactory.get(entityType.name());
        try {
            LOGGER.debug("Getting basic information : " + entityType.name());
            if (entityTypeManagerFactory.containsKey(entityType.name())) {
                LOGGER.debug("Found DAO information : " + entityType.name());

                return (IContentManager) manager.newInstance();

            }
        } catch (InstantiationException e) {
            String errorMessage = "can not instantiate class for entity : " + entityType.name()
                    + "  of " + manager.getName();
            LOGGER.error(errorMessage, e);
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, errorMessage);
        } catch (IllegalAccessException e) {
            String errorMessage = "can not instantiate class for entity : " + entityType.name()
                    + "  of " + manager.getName() + " due to illegal  access";

            LOGGER.error(errorMessage, e);
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, errorMessage);
        } catch (ClassCastException e) {
            String errorMessage = "can not instantiate class for entity : " + entityType.name()
                    + "  of " + manager.getName() + " due to illegal  access";

            LOGGER.error(errorMessage, e);
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, errorMessage);
        }

        return null;

    }
}