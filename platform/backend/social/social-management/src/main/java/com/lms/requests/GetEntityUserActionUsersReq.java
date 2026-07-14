package com.lms.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractOrgListReq;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class GetEntityUserActionUsersReq extends AbstractOrgListReq {

    public SrcEntity entity;
}
