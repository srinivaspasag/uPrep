package com.vedantu.comm.requests;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.ShareWithEntity;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.vedantu.content.pojos.Source;
import com.vedantu.content.pojos.SrcType.LinkType;

public class AddStatusFeedReq extends AbstractAuthCheckReq {

    @Required
    public String                orgId;
    public String                statusMessage;
    public Source                source;
    @Required
    public List<ShareWithEntity> with;

    @Override
    public String validate() {

        String superValidate = super.validate();
        if (null != superValidate) {
            return superValidate;
        }
        if (null == orgId) {
            return "orgId missing";
        }
        if (CollectionUtils.isEmpty(with)) {
            return "shared With empty";
        }
        if (source != null) {
            if (source.linkType == LinkType.ADDED && source.linkInfo == null) {
                return "invalid link info";
            }
        }
        return null;
    }
}
