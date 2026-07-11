package com.vedantu.commons.utils;

import java.util.Map;

import org.bson.types.ObjectId;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.DBObject;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.mongo.VedantuBaseMongoModel;

public class ObjectMapperUtils {
	private static final ALogger LOGGER = Logger.of(ObjectMapperUtils.class);

	private static ObjectMapper mapper = new ObjectMapper().configure(
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

	@SuppressWarnings({ "unchecked" })
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
