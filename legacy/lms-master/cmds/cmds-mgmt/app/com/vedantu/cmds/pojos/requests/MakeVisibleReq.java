package com.vedantu.cmds.pojos.requests;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import play.Logger;
import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class MakeVisibleReq extends AbstractAuthCheckReq {

    @Required
    public List<SrcEntity>               contents = new ArrayList<SrcEntity>();
    @Required
    public List<OrgContentVisibleOption> options = new ArrayList<OrgContentVisibleOption>();

    public String validate() {
        Logger.debug(".....Inside option validate function.........");
        if (options != null) {
            for (OrgContentVisibleOption option : options) {
                String value = option.validate();
                if (value != null) {
                    return value;
                }
            }
        } else {
            return "orgEntities missing";
        }
        if (CollectionUtils.isNotEmpty(contents)) {
            for (SrcEntity content : contents) {
                String value = content.validate();
                if (value != null) {
                    return value;
                }
            }
        } else {
            return "contents missing";
        }
        return null;
    }

}
