package com.vedantu.cmds.pojos.requests.accesscodes;

import com.vedantu.commons.pojos.requests.AbstractOrgListReq;

public class GetAccessCodesReq extends AbstractOrgListReq {
	public String sellerReferenceNo;
	public String pointOfSale;
	public String buyerEmail;
	public String forUserId;
}
