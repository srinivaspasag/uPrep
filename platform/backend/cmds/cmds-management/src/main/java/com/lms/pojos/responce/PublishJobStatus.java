package com.lms.pojos.responce;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;

public class PublishJobStatus implements IListResponseObj {

    public String jobId;
    public VedantuErrorCode errorCode;

    public PublishJobStatus() {
        super();
    }

    public PublishJobStatus(String jobId, VedantuErrorCode errorCode) {
        super();
        this.jobId = jobId;
        this.errorCode = errorCode;
    }

}
