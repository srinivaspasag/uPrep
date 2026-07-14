package com.lms.pojos.requests.analytics;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.pojo.request.AbstractOrgScopeReq;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
public class GetAttemptedEntitiesReq extends AbstractOrgScopeReq {
    public EntityType type;
    public List<String> ids;
    public long attemptedAfter;

    @NotBlank
    public String targetUserId;

    public String _getResultForUserId() {

        return StringUtils.isEmpty(targetUserId) ? userId : targetUserId;
    }
}
