package com.lms.pojos.content.question;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.enums.Scope;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.CmdsContentLinkType;

public class CMDSContentLinkInfo extends ModelBasicInfo implements IListResponseObj {

    public ModelBasicInfo source;
    public ModelBasicInfo target;
    public Scope scope;
    public boolean downloadable;
    public String downloadableState;
    public CmdsContentLinkType linkType;
    public long position;
    public long startsIn;
    public long endsIn;
    public long closesIn;

    public CMDSContentLinkInfo(String id, VedantuRecordState recordState, Scope scope) {

        super(id, recordState);
        this.scope = scope;
    }

    public void setSourceTarget(ModelBasicInfo source, ModelBasicInfo target) {

        this.source = source;
        this.target = target;
    }

}
