package com.vedantu.comm.managers.news.populators;

import java.util.Map;
import java.util.Set;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.news.details.DocumentNewsEntityDetails;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.Document;
import com.vedantu.mongo.IVedantuModel;

public class DocumenNewsPopulator extends AbstractEntityDetailsPopulator {

	public final static DocumenNewsPopulator INSTANCE = new DocumenNewsPopulator();
	private final static ALogger LOGGER = Logger.of(DocumenNewsPopulator.class);

	private DocumenNewsPopulator() {

	}

	@Override
	public void populate(String orgId, String userId,
			Set<SrcEntity> newsEntities,
			Map<SrcEntity, SrcEntity> srcEntityDetails, EntityType entityType) {

		super.populate(orgId, userId, newsEntities, srcEntityDetails,
				entityType);
	}

	@Override
	public SrcEntity populate(String orgId, String userId, SrcEntity newEntity,
			Map<String, IVedantuModel> modelDetailMap) {

		LOGGER.debug(" Populating document " + newEntity.id);
		Document file = (Document) modelDetailMap.get(newEntity.id);
		if (file == null) {
			LOGGER.error("no document found for : " + newEntity);
			return null;
		}
		DocumentNewsEntityDetails details = new DocumentNewsEntityDetails(
				newEntity.id);

		details.name = file.name;
		details.id = file._getStringId();
		details.type = EntityType.DOCUMENT;
		details.timeCreated = file.timeCreated;
		details.contentSrc = file.contentSrc;
		return details;

	}
}
