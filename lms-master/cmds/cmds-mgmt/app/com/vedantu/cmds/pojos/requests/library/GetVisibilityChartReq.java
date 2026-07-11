package com.vedantu.cmds.pojos.requests.library;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetVisibilityChartReq extends AbstractAuthCheckReq {

    @Required
    public SrcEntity content;
    @Required 
    public String orgId;
    @Required
    public int       start;
    public int       size;

    public String validate() {

        if (content == null) {
            return "invalid content request";

        } else if ((StringUtils.isNotEmpty(content.id) && content.type == EntityType.UNKNOWN)
                || content.type == null) {
            return "invalid content request";
        }
        return null;
    }
}
