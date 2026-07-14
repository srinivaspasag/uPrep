package com.lms.common.news;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class NewsUtils {

	private static final Logger logger = LoggerFactory.getLogger(NewsUtils.class);
	// disambiguation in case of lots of events happen at the same instance
	private static final int MAX_RAND = 99999;
	private static final int ROW_ID_COMPONENT_COUNT = 3;

	public static Map<String, SrcEntity> getSrcEntityIds(Set<SrcEntity> newsEntities) {

		Map<String, SrcEntity> entityIds = new HashMap<String, SrcEntity>();
		for (SrcEntity entity : newsEntities) {
			String id = entity.id;
			logger.debug(" Entity Details from collected news Entities:" + entity.id + " type " + entity.type);
			entityIds.put(id, entity);
		}
		return entityIds;
	}

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

	public static boolean isValidRowId(String rowId) {

		String[] tokens = rowId.split("_");
		if (tokens.length < ROW_ID_COMPONENT_COUNT) {
			logger.error("invalid no. of id-components (" + tokens.length + ") in rowId : " + rowId);
			return false;
		}

		final String entityId = tokens[0];
		final String entityType = tokens[1];
		final String timeValue = tokens[2];

		if (entityId == null || entityId.isEmpty()) {
			logger.error("invalid user in rowId : " + rowId);
			return false;
		}

		if (!EntityType.isValidQualifierChar(entityType)) {
			logger.error("invalid entityType (" + entityType + ") in rowId : " + rowId);
			return false;
		}
		if (timeValue == null || timeValue.isEmpty()) {
			logger.error("invalid timeValue (" + timeValue + ") in rowId : " + rowId);
			return false;
		}

		return true;
	}
}
