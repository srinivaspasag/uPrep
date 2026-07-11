package com.vedantu.comm.requests.newsfeeed;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetActivityFeedsReq extends AbstractAuthCheckReq {

	@Required
	public EntityType	eType;

	@Required
	public String		eId;

	@Required
	public int			size;
	public boolean		needClustered;
	public List<String>	userActions;
	public String		orgId;
	// public String validate(){
	// if( size < 0 ){
	// return "invalid size";
	// }
	// if( StringUtils.isEmpty(tid)){
	// return "invalid id";
	// }
	// if( type == EntityType.UNKNOWN ){
	// return "invalid entity type";
	// }
	// return null;
	// }

}
