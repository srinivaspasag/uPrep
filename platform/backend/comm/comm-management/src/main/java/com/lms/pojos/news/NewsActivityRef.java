package com.lms.pojos.news;

import com.lms.common.hbase.AbstractHbaseModels;
import com.lms.enums.NotificationReason;

public class NewsActivityRef extends AbstractHbaseModels {

	public String aid;
	public NotificationReason why;

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
