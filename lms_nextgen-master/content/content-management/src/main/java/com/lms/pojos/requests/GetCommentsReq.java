package com.lms.pojos.requests;

import com.lms.api.IRequestParamsValidator;
import com.lms.common.exception.VedantuException;
import com.lms.common.utils.ObjectIdUtils;
import com.lms.common.vedantu.commons.pojos.requests.AbstractOrgListReq;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class GetCommentsReq extends AbstractOrgListReq implements IRequestParamsValidator {

    @NotBlank(message = "parent should not be null")
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
