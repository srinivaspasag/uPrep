package com.vedantu.comm.requests.newsfeeed;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetOlderActivityFeedsReq extends AbstractAuthCheckReq {

    @Required
    public EntityType   eType;
    @Required
    public String       eId;
    @Required
    public String       beforeNewsActivityId;
    @Required
    public int          size;
    public boolean      needClustered;
    public List<String> userActions;
    public String       orgId;

    public String validate() {

        if (size < 0) {
            return "invalid size";
        }
        if (StringUtils.isEmpty(eId)) {
            return "invalid id";
        }
        if (eType == EntityType.UNKNOWN) {
            return "invalid entity type";
        }
        if (StringUtils.isEmpty(beforeNewsActivityId)) {
            return "invalid beforeNewsActivityId";
        }
        return null;
    }
}
