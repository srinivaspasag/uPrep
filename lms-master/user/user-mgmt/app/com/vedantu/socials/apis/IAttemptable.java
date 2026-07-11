package com.vedantu.socials.apis;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.mongo.VedantuBaseMongoModel;

public interface IAttemptable {
	public String FIELD_NAME = ConstantsGlobal.ATTEMPTS;

	public VedantuBaseMongoModel incAttemptsCount(String id, int inc);
}
