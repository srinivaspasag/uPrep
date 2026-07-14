package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.Scope;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public abstract class AbstractAddContentReq extends AbstractAuthCheckReq {

    public List<String> tags;
    public Scope scope;
    public SrcEntity contentSrc;

}
