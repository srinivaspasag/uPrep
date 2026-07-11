package com.lms.pojos.requests.analytics;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.pojo.request.AbstractOrgScopeReq;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class GetUserEntityAnalyticsReq extends AbstractOrgScopeReq {
    @NotBlank(message = "entity cannot be empty")
    public SrcEntity entity;
    public SrcEntity target;

    @NotBlank(message = "targetUserId cannot be empty")
    public String targetUserId;

    public String _getResultForUserId() {

        return StringUtils.isEmpty(targetUserId) ? userId : targetUserId;
    }
}
