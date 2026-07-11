package com.vedantu.comm.managers.news.populators;

import java.util.HashMap;
import java.util.Map;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;

public class EntityNewsDetailsFactory {
	private static final ALogger			LOGGER		= Logger.of(EntityNewsDetailsFactory.class);
	public static EntityNewsDetailsFactory	INSTANCE	= new EntityNewsDetailsFactory();

	private EntityNewsDetailsFactory() {

	}

	private Map<EntityType, Class<? extends SrcEntity>>	entityDetailsMap	= new HashMap<EntityType, Class<? extends SrcEntity>>();

	public boolean register(EntityType entityType,
			Class<? extends SrcEntity> detailsClazz) {
		entityDetailsMap.put(entityType, detailsClazz);
		return true;

	}

	public SrcEntity getInstance(EntityType entityType) {
		LOGGER.debug("Getting basic information : " + entityType.name());
		SrcEntity newDetails = null;
		try {
			if (entityDetailsMap.containsKey(entityType)) {
				LOGGER.debug("Found EntityDetails information : " + entityType.name());
				Class<? extends SrcEntity> entityNewsDetailsClazz = entityDetailsMap
						.get(entityType);

				newDetails = entityNewsDetailsClazz.newInstance();
			}
		} catch (InstantiationException e) {
			   LOGGER.error("can not instantiate", e);
		} catch (IllegalAccessException e) {
		    LOGGER.error("Illegal not instantiate", e);
		}
		if (newDetails == null)
			LOGGER.debug("No entity details class found  information : "
					+ entityType);
		return newDetails;

	}
}
