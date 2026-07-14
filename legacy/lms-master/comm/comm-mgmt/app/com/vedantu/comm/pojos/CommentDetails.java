package com.vedantu.comm.pojos;

import com.vedantu.comm.news.details.CommentNewsEntityDetails;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;

public class CommentDetails extends CommentNewsEntityDetails {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	SrcEntity	parentDetails;

	public CommentDetails(EntityType type, String id) {
		super(type, id);
	}

}
