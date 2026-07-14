package com.vedantu.comm.pojos.news;

import com.vedantu.comm.enums.NotificationReason;
import com.vedantu.commons.hbase.AbstractHbaseModels;

public class NewsActivityRef extends AbstractHbaseModels {

	public String				aid;
	public NotificationReason	why;

	public NewsActivityRef() {
	}

	public NewsActivityRef(String aid, NotificationReason why) {
		this.aid = aid;
		this.why = why;
	}

	@Override
	public String getKey() {
		// TODO Auto-generated method stub
		return null;
	}

}
