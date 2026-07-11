package com.vedantu.cmds.pojos.responses.files;

import java.util.HashMap;
import java.util.Map;

import com.vedantu.cmds.models.event.search.details.CMDSFileSearchIndexDetails;
import com.vedantu.commons.enums.OperationType;

public class GetCMDSFileRes extends CMDSFileSearchIndexDetails {

    public Map<OperationType, String> operationJobIdMap;

    public GetCMDSFileRes() {

        super();
        operationJobIdMap = new HashMap<OperationType, String>();
    }
}
