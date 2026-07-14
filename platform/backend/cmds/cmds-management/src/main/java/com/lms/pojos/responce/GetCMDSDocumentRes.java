package com.lms.pojos.responce;

import com.lms.common.vedantu.enums.OperationType;
import com.lms.models.event.search.details.CMDSDocumentSearchIndexDetails;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;


@Getter
@Setter
public class GetCMDSDocumentRes  extends CMDSDocumentSearchIndexDetails
{
    public Map<OperationType, String> operationJobIdMap;

    public GetCMDSDocumentRes() {

        super();
        operationJobIdMap = new HashMap<OperationType, String>();
    }
}
