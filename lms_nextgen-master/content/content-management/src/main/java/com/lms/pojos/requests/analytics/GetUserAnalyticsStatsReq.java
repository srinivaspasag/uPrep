package com.lms.pojos.requests.analytics;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.enums.EntityType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class GetUserAnalyticsStatsReq extends AbstractAuthCheckReq {

    @NotBlank(message = "entitytype cannot be empty")
    public EntityType entityType;

    public String targetUserId;

    public String _getResultForUserId() {
        return StringUtils.isEmpty(targetUserId) ? userId : targetUserId;
    }
}
