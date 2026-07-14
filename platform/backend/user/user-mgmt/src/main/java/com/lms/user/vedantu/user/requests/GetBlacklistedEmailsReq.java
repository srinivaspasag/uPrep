package com.lms.user.vedantu.user.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractListReq;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetBlacklistedEmailsReq extends AbstractListReq {

    public String query;
}
