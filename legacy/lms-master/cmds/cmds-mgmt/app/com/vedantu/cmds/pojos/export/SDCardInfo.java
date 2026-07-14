package com.vedantu.cmds.pojos.export;

import com.vedantu.commons.enums.EncryptionLevel;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.mongo.VedantuRecordState;

public class SDCardInfo extends ModelBasicInfo {

    public SrcEntity       target;
    public String          groupId;
    public long            contentSize;

    public long            maxSize;
    public EncryptionLevel encLevel;
    private String         name;
    public long            count;

    public SDCardInfo(String id, VedantuRecordState recordState) {

        super(id, recordState);
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getName() {

        return name;
    }

}
