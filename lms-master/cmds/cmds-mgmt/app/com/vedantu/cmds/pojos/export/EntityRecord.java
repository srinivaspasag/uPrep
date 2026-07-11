package com.vedantu.cmds.pojos.export;

import com.vedantu.commons.pojos.SrcEntity;

public class EntityRecord {

    public EntityRecord() {

    }

    public EntityRecord(SrcEntity content) {

        this.content = content;
    }

    public SrcEntity content;
    public long      exportedSize;
    public String    errorCode;
    
    @Override
    public String toString() {

        return "EntityRecord [content=" + content + ", exportedSize=" + exportedSize
                + ", errorCode=" + errorCode + "]";
    }
}
