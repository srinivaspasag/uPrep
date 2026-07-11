package com.vedantu.content.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.requests.AbstractOrgListReq;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.content.apis.IRequestParamsValidator;

public class GetSimilarEntities extends AbstractOrgListReq implements
		IRequestParamsValidator {

	@Required
	public SrcEntity entity;

	@Override
	public boolean validateRequestParams() throws VedantuException {

		if (entity == null || ObjectIdUtils.hasInvalidId(entity.id)
				|| entity.type == null || entity.type == EntityType.UNKNOWN) {
			return false;
		}
		return true;
	}
}
