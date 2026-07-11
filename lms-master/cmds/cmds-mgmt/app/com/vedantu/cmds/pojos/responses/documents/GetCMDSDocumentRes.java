package com.vedantu.cmds.pojos.responses.documents;

import java.util.HashMap;
import java.util.Map;

import com.vedantu.cmds.models.event.search.details.CMDSDocumentSearchIndexDetails;
import com.vedantu.commons.enums.OperationType;

public class GetCMDSDocumentRes extends CMDSDocumentSearchIndexDetails {

    public Map<OperationType, String> operationJobIdMap;

    public GetCMDSDocumentRes() {

        super();
        operationJobIdMap = new HashMap<OperationType, String>();
    }

}
