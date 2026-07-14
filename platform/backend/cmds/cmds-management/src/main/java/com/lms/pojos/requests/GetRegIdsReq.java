package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractOrgListReq;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetRegIdsReq extends AbstractOrgListReq {
    public List<String> programNames;

}
