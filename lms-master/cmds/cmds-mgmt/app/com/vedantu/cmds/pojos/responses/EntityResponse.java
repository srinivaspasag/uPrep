package com.vedantu.cmds.pojos.responses;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;

public class EntityResponse implements IListResponseObj {

    public SrcEntity        content;
    public ModelBasicInfo   orgEntity;
    public VedantuErrorCode errorCode;
    public boolean          success;

    public EntityResponse(SrcEntity entity, VedantuErrorCode errorCode) {

        super();
        this.content = entity;
        this.errorCode = errorCode;
        this.success = errorCode != null ? false : true;
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
