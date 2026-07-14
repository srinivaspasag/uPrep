package com.vedantu.comm.news.details;

import com.vedantu.comm.managers.news.populators.EntityNewsDetailsFactory;
import com.vedantu.commons.ShareWithEntity;
import com.vedantu.commons.enums.EntityType;

public class ProgramNewsEntityDetails extends ShareWithEntity {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	public String				name;
	public String				code;

	static {
		EntityNewsDetailsFactory.INSTANCE.register(EntityType.PROGRAM,
				ProgramNewsEntityDetails.class);
	}

	public ProgramNewsEntityDetails(EntityType type, String id) {
		super(type, id);
		// TODO Auto-generated constructor stub
	}
}
