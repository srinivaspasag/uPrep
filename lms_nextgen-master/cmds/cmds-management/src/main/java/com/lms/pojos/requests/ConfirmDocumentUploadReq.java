package com.lms.pojos.requests;

import com.lms.enums.DisplayOrientation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ConfirmDocumentUploadReq extends AbstractConfirmFileModelReq {

    public DisplayOrientation orientation;

    public String docId;

    public String testId;

}
