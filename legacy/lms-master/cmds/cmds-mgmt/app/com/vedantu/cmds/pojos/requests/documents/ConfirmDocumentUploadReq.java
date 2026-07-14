package com.vedantu.cmds.pojos.requests.documents;

import com.vedantu.cmds.pojos.requests.AbstractConfirmFileModelReq;
import com.vedantu.commons.enums.DisplayOrientation;

public class ConfirmDocumentUploadReq extends AbstractConfirmFileModelReq {

    public DisplayOrientation orientation;

    public String             docId;

    public String             testId;

}
