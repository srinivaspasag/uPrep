package com.lms.common.utils;

import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class ObjectMapperUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(ObjectMapperUtils.class);

	private static final ObjectMapper mapper = new ObjectMapper().configure(
			DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	public static ObjectMapper mapper() {

		return mapper;
	}

	public static <T> T convertValue(Object fromValue, Class<T> toValueType) {
		if (fromValue != null) {
			return mapper().convertValue(fromValue, toValueType);
		} else
			return null;
	}

	@SuppressWarnings({"unchecked"})
	public static <T extends VedantuBaseMongoModel> T convertToVedantuBaseModel(
			DBObject dbObject, Class<T> toValueType) {
		if (dbObject == null) {
			return null;
		}
		Map<String, Object> map = dbObject.toMap();
		ObjectId id = null;
		Object removedObj = map.remove(ConstantsGlobal._ID);
		try {
			id = (ObjectId) removedObj;
		} catch (Exception e) {

			LOGGER.error(e.getMessage());
			id = new ObjectId(removedObj.toString());
		}
		LOGGER.debug("objectId : " + id);
		if (id == null) {
			return null;
		}
		T model = ObjectMapperUtils.convertValue(map, toValueType);
		model.id = id;
		return model;
	}

}
