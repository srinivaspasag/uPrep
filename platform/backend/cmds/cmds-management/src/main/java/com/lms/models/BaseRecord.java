package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EncryptionLevel;
import org.springframework.data.annotation.Transient;

public class BaseRecord extends AbstractBoardEntityTagModel {
    @Transient
    public static final String JOB_ID = "jobId";

    public SrcEntity target;
    public EncryptionLevel encLevel;
    public String targetUserId;
    public String jobId;
}
