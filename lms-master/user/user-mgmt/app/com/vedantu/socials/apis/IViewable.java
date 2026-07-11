package com.vedantu.socials.apis;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.mongo.VedantuBaseMongoModel;

public interface IViewable {

	public String FIELD_NAME = ConstantsGlobal.VIEWS;

	public VedantuBaseMongoModel incViewsCount(String id, int inc);
}
