package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractOrgListReq;
import com.lms.common.vedantu.enums.EntityType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class GetUserEntityResultAnalyticsReq extends AbstractOrgListReq {

    @NotBlank(message = "entityType should not be null")
    public EntityType entityType;
    public String targetUserId;

    public String __getResultForUserId() {
        return !StringUtils.isEmpty(targetUserId) ? targetUserId : userId;
    }

}
