package com.vedantu.cmds.mgmt.publishers;

import java.util.HashMap;
import java.util.Map;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.enums.EntityType;

public class EntityTypePublisherFactory {
	private static final ALogger LOGGER = Logger
			.of(EntityTypePublisherFactory.class);
	public static EntityTypePublisherFactory INSTANCE = new EntityTypePublisherFactory();

	private EntityTypePublisherFactory() {

	}

	private Map<EntityType, IPublisher> cmdsPublisherMap = new HashMap<EntityType, IPublisher>();

	public boolean register(EntityType entityType, IPublisher basicPublisher) {
		cmdsPublisherMap.put(entityType, basicPublisher);
		return true;

	}

	public IPublisher get(EntityType entityType) {
		LOGGER.debug("Getting basic information : " + entityType.name());
		if (cmdsPublisherMap.containsKey(entityType)) {
			LOGGER.debug("Found DAO information : " + entityType.name());
			return cmdsPublisherMap.get(entityType);
		}

		LOGGER.debug("No publisher found information : " + entityType);
		return null;

	}
}
