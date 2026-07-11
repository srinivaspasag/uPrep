package com.vedantu.cmds.pojos.requests.accesscodes;

import play.data.validation.Constraints.Required;

import com.vedantu.cmds.enums.ShipmentStatus;

public class UpdateShipmentStatusReq {
	@Required
	public String accessCodeId;
	public ShipmentStatus shipmentStatus;
}
