package com.vedantu.ext.cmds.pojo.responses.local;

import java.io.Serializable;

public class GetPackResponse implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private int               jobId;
    private String            sdCardId;
    private String            location;

    public GetPackResponse(int jobId, String sdCardId,String location) {

        super();
        this.jobId = jobId;
        this.sdCardId = sdCardId;
        this.location= location;
    }

    public int getJobId() {

        return jobId;
    }

    public void setJobId(int jobId) {

        this.jobId = jobId;
    }

    public String getSdCardId() {

        return sdCardId;
    }

    public void setSdCardId(String sdCardId) {

        this.sdCardId = sdCardId;
    }

    
    public String getLocation() {
    
        return location;
    }

    
    public void setLocation(String location) {
    
        this.location = location;
    }

    @Override
    public String toString() {

        return "GetPackResponse [jobId=" + jobId + ", sdCardId=" + sdCardId + ", location"+ location +"]";
    }

}
