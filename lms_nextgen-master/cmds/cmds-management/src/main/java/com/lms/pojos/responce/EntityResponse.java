package com.lms.pojos.responce;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

@Getter
@Setter
public class EntityResponse implements IListResponseObj {

    public SrcEntity content;
    public ModelBasicInfo orgEntity;
    public VedantuErrorCode errorCode;
    public boolean success;

    public EntityResponse(SrcEntity entity, VedantuErrorCode errorCode) {

        super();
        this.content = entity;
        this.errorCode = errorCode;
        this.success = errorCode == null;
    }

    public static VedantuErrorCode getCumulativeErrorCode(List<EntityResponse> responses) {

        if (CollectionUtils.isEmpty(responses)) {
            return null;
        }
        for (EntityResponse response : responses) {
            if (response.errorCode != null) {
                return response.errorCode;
            }
        }
        return null;
    }

}
