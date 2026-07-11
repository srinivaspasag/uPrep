package com.vedantu.socials.apis;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.mongo.VedantuBaseMongoModel;

public interface IFollowable {

	public String FIELD_NAME = ConstantsGlobal.FOLLOWERS;

	public VedantuBaseMongoModel incFollowersCount(String id, int inc);
}
