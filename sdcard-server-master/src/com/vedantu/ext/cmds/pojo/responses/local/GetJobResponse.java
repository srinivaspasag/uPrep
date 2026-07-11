package com.vedantu.ext.cmds.pojo.responses.local;

import java.io.Serializable;

public class GetJobResponse implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private int               jobId;
    private long              completed;            // total completed steps
    private long              steps;            // total completed steps
    private String            status;
    private String            targetId;
    private String            targetType;

    public GetJobResponse(int jobId ) {

        super();
        this.jobId = jobId;
    
    }

    public int getJobId() {

        return jobId;
    }

    public void setJobId(int jobId) {

        this.jobId = jobId;
    }

    
    public long getCompleted() {
    
        return completed;
    }

    
    public void setCompleted(long completed) {
    
        this.completed = completed;
    }

    
    public String getStatus() {
    
        return status;
    }

    
    public void setStatus(String status) {
    
        this.status = status;
    }

    
    public String getTargetId() {
    
        return targetId;
    }

    
    public void setTargetId(String targetId) {
    
        this.targetId = targetId;
    }

    
    public String getTargetType() {
    
        return targetType;
    }

    
    public void setTargetType(String targetType) {
    
        this.targetType = targetType;
    }

    
    public long getSteps() {
    
        return steps;
    }

    
    public void setSteps(long steps) {
    
        this.steps = steps;
    }

    
}
