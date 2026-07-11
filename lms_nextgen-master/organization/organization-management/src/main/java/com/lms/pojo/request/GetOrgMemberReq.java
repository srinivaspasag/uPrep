package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAppCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class GetOrgMemberReq extends AbstractAppCheckReq {

    @NotBlank(message = "orgId should not be null")
    public String  orgId;

    @NotBlank(message = "memberId should not be null")
    public String  memberId;

    public boolean ensureCourseInfo;

    public boolean getKey;
    public boolean loginStatusRequested;

    public GetOrgMemberReq() {
        super();
    }

    public GetOrgMemberReq(String orgId, String memberId) {
        super();
        this.orgId = orgId;
        this.memberId = memberId;
    }
}