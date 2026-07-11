package com.vedantu.cmds.pojos.responses.videos;

import java.util.HashMap;
import java.util.Map;

import com.vedantu.cmds.models.event.search.details.CMDSVideoSearchIndexDetails;
import com.vedantu.commons.enums.OperationType;

public class GetCMDSVideoRes extends CMDSVideoSearchIndexDetails {

    public Map<OperationType, String> operationJobIdMap;

    public GetCMDSVideoRes() {

        super();
        operationJobIdMap = new HashMap<OperationType, String>();
    }
}
