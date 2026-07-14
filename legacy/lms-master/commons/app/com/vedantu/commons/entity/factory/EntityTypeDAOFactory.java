package com.vedantu.commons.entity.factory;

import java.util.HashMap;
import java.util.Map;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBasicDAO;

public class EntityTypeDAOFactory {

	private static final ALogger LOGGER = Logger.of(EntityTypeDAOFactory.class);
	public static EntityTypeDAOFactory INSTANCE = new EntityTypeDAOFactory();

	@SuppressWarnings("rawtypes")
	private Map<String, VedantuBasicDAO> entityTypeDAOFactory = new HashMap<String, VedantuBasicDAO>();

	private EntityTypeDAOFactory() {

	}

	@SuppressWarnings("rawtypes")
	public boolean register(EntityType entityType, VedantuBasicDAO basicDAO) {
		entityTypeDAOFactory.put(entityType.name(), basicDAO);
		return true;

	}

	@SuppressWarnings("rawtypes")
	public VedantuBasicDAO get(EntityType entityType) {
		LOGGER.debug("Getting basic information : " + entityType.name());
		if (entityTypeDAOFactory.containsKey(entityType.name())) {
			LOGGER.debug("Found DAO information : " + entityType.name());
			return entityTypeDAOFactory.get(entityType.name());
		}

		LOGGER.debug("No DAO found information : " + entityType.name());
		return null;

	}

	@SuppressWarnings("rawtypes")
	public String getOwnerId(SrcEntity entity) {
		if (entity == null) {
			return null;
		}
		VedantuBasicDAO dao = get(entity.type);
		if (dao == null) {
			return null;
		}
		return dao.getOwnerId(entity.id);
	}
}