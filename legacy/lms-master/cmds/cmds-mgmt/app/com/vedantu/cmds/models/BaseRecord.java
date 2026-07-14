package com.vedantu.cmds.models;

import com.google.code.morphia.annotations.Transient;
import com.vedantu.commons.enums.EncryptionLevel;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.AbstractBoardEntityTagModel;

public abstract class BaseRecord extends AbstractBoardEntityTagModel {

    @Transient
    public static final String JOB_ID = "jobId";

    public SrcEntity           target;
    public EncryptionLevel     encLevel;
    public String              targetUserId;
    public String              jobId;
}
