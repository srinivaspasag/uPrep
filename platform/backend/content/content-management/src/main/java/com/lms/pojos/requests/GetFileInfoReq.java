package com.lms.pojos.requests;

import java.util.List;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.pojo.request.AbstractOrgScopeReq;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetFileInfoReq extends AbstractOrgScopeReq {

    
    public List<SrcEntity> contents;
}
