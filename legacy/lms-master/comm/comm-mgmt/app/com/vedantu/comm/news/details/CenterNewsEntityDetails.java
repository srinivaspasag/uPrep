package com.vedantu.comm.news.details;

import com.vedantu.comm.managers.news.populators.EntityNewsDetailsFactory;
import com.vedantu.commons.ShareWithEntity;
import com.vedantu.commons.enums.EntityType;

public class CenterNewsEntityDetails extends ShareWithEntity {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	public String				name;
	public String				code;
	static {
		EntityNewsDetailsFactory.INSTANCE.register(EntityType.CENTER,
				CenterNewsEntityDetails.class);
	}

	public CenterNewsEntityDetails(EntityType type, String id) {
		super(type, id);
	}
}
