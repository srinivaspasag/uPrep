package com.vedantu.cmds.pojos.responses;

import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;

public class GetExportRecordRes implements IListResponseObj {

    public ModelBasicInfo recordInfo;
    public String         jobId;
    public String         url;
    @Override
    public String toString() {

        return "ExportRecordRes [recordInfo=" + recordInfo + ", jobId=" + jobId + ", toString()="
                + super.toString() + "]";
    }

}