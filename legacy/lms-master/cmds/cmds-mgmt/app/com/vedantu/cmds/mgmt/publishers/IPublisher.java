package com.vedantu.cmds.mgmt.publishers;

import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;

public interface IPublisher {

	public boolean publish(String userId, String orgId, SrcEntity content,
			String jobId) throws VedantuException;


	public void prePublish(SrcEntity content);
	
	public void postPublish(VedantuBaseMongoModel model);
}
