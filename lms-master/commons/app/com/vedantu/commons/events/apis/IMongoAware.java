package com.vedantu.commons.events.apis;

import com.vedantu.mongo.VedantuBaseMongoModel;

public interface IMongoAware {
	public void fromMongoModel(VedantuBaseMongoModel mongoModel);

}
