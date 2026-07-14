package com.vedantu.cmds.pojos.content.question;

import com.vedantu.cmds.enums.CmdsContentLinkType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.mongo.VedantuRecordState;

public class CMDSContentLinkInfo extends ModelBasicInfo implements IListResponseObj {

    public ModelBasicInfo      source;
    public ModelBasicInfo      target;
    public Scope               scope;
    public boolean             downloadable;
    public String              downloadableState;
    public CmdsContentLinkType linkType;
    public long                position;
    public long                startsIn;
    public long                endsIn;
    public long                closesIn;

    public CMDSContentLinkInfo(String id, VedantuRecordState recordState, Scope scope) {

        super(id, recordState);
        this.scope = scope;
    }

    public void setSourceTarget(ModelBasicInfo source, ModelBasicInfo target) {

        this.source = source;
        this.target = target;
    }

}
