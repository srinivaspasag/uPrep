package com.vedantu.cmds.pojos.export;

import com.google.code.morphia.annotations.Embedded;
import com.vedantu.commons.pojos.SrcEntity;

@Embedded
public class EntityExportRecord extends EntityRecord {

    public long    timeExported;
    public boolean succeeded = false;
    public long    timeAdded;

    public EntityExportRecord() {

    }

    public EntityExportRecord(SrcEntity content, long timeAdded) {

        super(content);
        this.timeAdded = timeAdded;
    }

    @Override
    public String toString() {

        return "EntityExportRecord [timeExported=" + timeExported + ", succeeded=" + succeeded
                + ", timeAdded=" + timeAdded + "]";
    }

}
