package com.vedantu.socials.apis;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.mongo.VedantuBaseMongoModel;

public interface ICommentable {

	public String FIELD_NAME = ConstantsGlobal.COMMENTS;

	public VedantuBaseMongoModel incCommentsCount(String id);
}
