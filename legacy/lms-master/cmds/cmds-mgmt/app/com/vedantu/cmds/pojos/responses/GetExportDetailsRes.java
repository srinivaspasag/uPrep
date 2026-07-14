package com.vedantu.cmds.pojos.responses;

import java.util.List;

import com.vedantu.cmds.pojos.export.EntityExportRecordInfo;

public class GetExportDetailsRes extends GetExportRecordRes {

    public long                         totalHits;
    public List<EntityExportRecordInfo> contentInfo;
}
