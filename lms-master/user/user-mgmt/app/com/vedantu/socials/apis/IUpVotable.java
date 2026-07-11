package com.vedantu.socials.apis;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.mongo.VedantuBaseMongoModel;

public interface IUpVotable {
	public String FIELD_NAME = ConstantsGlobal.UP_VOTES;

	public VedantuBaseMongoModel incUpVotesCount(String id);

}
