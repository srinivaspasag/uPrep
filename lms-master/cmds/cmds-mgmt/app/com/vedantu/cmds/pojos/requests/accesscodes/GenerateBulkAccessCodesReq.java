package com.vedantu.cmds.pojos.requests.accesscodes;

import java.util.List;

import play.Logger;
import play.data.validation.Constraints.Required;

import com.vedantu.cmds.enums.ShipmentStatus;
import com.vedantu.cmds.pojos.ContactDetails;
import com.vedantu.cmds.pojos.SellerInfo;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class GenerateBulkAccessCodesReq extends AbstractOrgScopeReq {
	@Required
	public ContactDetails buyerContactDetails;
	@Required
	public SellerInfo sellerInfo;
	@Required
	public List<SrcEntity> entities;
	public ShipmentStatus shipmentStatus;
	public int count;

	public String validate() {
		Logger.debug(".....Inside buyerContactDetails validate function.........");

		if (buyerContactDetails == null) {
			return "buyerContactDetails is null";
		}

		if (sellerInfo == null) {
			return "sellerInfo is null";
		}

		else if (sellerInfo.pointOfSale == null) {
			return "pointOfSale is null";
		}

		else if (sellerInfo.sellerReferenceNo == null) {
			return "sellerReferenceNo is null";
		}

		return null;
	}
}