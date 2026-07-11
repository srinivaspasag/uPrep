package com.lms.pojos.requests;

import com.lms.api.IRequestParamsValidator;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.commons.pojos.requests.AbstractOrgListReq;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetSimilarEntities extends AbstractOrgListReq implements
        IRequestParamsValidator {

    //@NotEmpty(message = "entity should not be null")
    public SrcEntity entity;

    @Override
    public boolean validateRequestParams() throws VedantuException {

     /*   return entity != null && !ObjectIdUtils.hasInvalidId(entity.id)
                && entity.type != null && entity.type != EntityType.UNKNOWN;*/
        return true;
    }
}
