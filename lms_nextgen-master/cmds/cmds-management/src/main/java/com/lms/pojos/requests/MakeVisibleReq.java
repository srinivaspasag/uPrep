package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.pojos.OrgContentVisibleOption;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class MakeVisibleReq extends AbstractAuthCheckReq {

    @NotBlank
    public List<SrcEntity> contents = new ArrayList<SrcEntity>();
    @NotBlank
    public List<OrgContentVisibleOption> options = new ArrayList<OrgContentVisibleOption>();

    public String validate() {
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
                /*String value = content.validate();
               if (value != null) {
                    return value;
                }*/
            }
        } else {
            return "contents missing";
        }
        return null;
    }

}
