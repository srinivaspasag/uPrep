package com.lms.pojos.responce;

import com.lms.common.vedantu.enums.OperationType;
import com.lms.models.event.search.details.CMDSVideoSearchIndexDetails;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class GetCMDSVideoRes extends CMDSVideoSearchIndexDetails {

    public Map<OperationType, String> operationJobIdMap;

    public GetCMDSVideoRes() {

        super();
        operationJobIdMap = new HashMap<OperationType, String>();
    }
}