package com.vedantu.cmds.pojos.export;

import com.google.code.morphia.annotations.Embedded;
import com.vedantu.commons.pojos.SrcEntity;

@Embedded
public class EntityExportRecordInfo {

    public EntityExportRecordInfo() {

    }

    public EntityExportRecordInfo(String name, EntityExportRecord entityExportRecord) {

        super();
        this.content = entityExportRecord.content;
        this.name = name != null ? name : "";
        this.timeExported = entityExportRecord.timeExported;
        this.exportedSize = entityExportRecord.exportedSize;
        this.errorCode = entityExportRecord.errorCode;
        this.succeeded = entityExportRecord.succeeded;
    }

    public String    name;
    public SrcEntity content;
    public long      timeExported;
    public long      exportedSize;
    public boolean   succeeded = false;
    public String    errorCode;

}
