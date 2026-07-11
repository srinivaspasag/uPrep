package com.lms.requests;

import com.lms.common.ShareWithEntity;
import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.pojos.Source;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
public class AddStatusFeedReq extends AbstractAuthCheckReq {

    @NotBlank
    public String orgId;
    public String statusMessage;
    public Source source;
    @NotBlank
    public List<ShareWithEntity> with;

  /*  @Override
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
    }*/
}

