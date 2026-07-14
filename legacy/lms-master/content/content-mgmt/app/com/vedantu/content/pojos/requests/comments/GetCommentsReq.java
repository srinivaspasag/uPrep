package com.vedantu.content.pojos.requests.comments;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.requests.AbstractOrgListReq;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.content.apis.IRequestParamsValidator;

public class GetCommentsReq extends AbstractOrgListReq implements IRequestParamsValidator {

    @Required
    public SrcEntity parent;

    @Override
    public boolean validateRequestParams() throws VedantuException {

        if (parent == null || ObjectIdUtils.hasInvalidId(parent.id) || parent.type == null
                || parent.type == EntityType.UNKNOWN) {
            return false;
        }
        return true;
    }
}
