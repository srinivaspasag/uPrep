package com.lms.pojos;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import org.apache.tomcat.util.net.openssl.ciphers.EncryptionLevel;

public class SDCardInfo extends ModelBasicInfo {
    public SrcEntity target;
    public String groupId;
    public long contentSize;

    public long maxSize;
    public EncryptionLevel encLevel;
    public long count;
    private String name;

    public SDCardInfo(String id, VedantuRecordState recordState) {

        super(id, recordState);
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }
}
