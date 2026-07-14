package com.vedantu.comm.utils;


import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.vedantu.commons.pojos.SrcEntity;

public class NewsEntitySerializer implements JsonSerializer<SrcEntity> {

	@Override
	public JsonElement serialize(SrcEntity newEntity, Type arg1, JsonSerializationContext arg2) {
		// TODO Auto-generated method stub
		return null;
//		
//		return newEntity.toJsonElement();
	}

}
