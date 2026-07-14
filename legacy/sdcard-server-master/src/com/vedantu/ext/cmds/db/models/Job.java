package com.vedantu.ext.cmds.db.models;

import com.vedantu.ext.cmds.enums.JobState;

public class Job extends AbstractDBModel {

    private static final long  serialVersionUID  = 1L;
    public static final String FIELD_COMPLETED   = "completed";
    public static final String FIELD_STEPS       = "steps";
    public static final String FIELD_STATUS      = "status";
    public static final String FIELD_TARGET_ID   = "targetId";
    public static final String FIELD_TARGET_TYPE = "targetType";

    public long                steps;                           // total steps
    public long                completed;                       // total completed steps
    public String              status;
    public String              targetId;
    public String              targetType;

    public Job() {

        super();
    }

    public Job(int orgKeyId, long steps, String targetId, String targetType) {

        super(orgKeyId);
        this.status = JobState.STARTED.name();
        this.steps = steps;
        this.targetId = targetId;
        this.targetType = targetType;
    }

}
