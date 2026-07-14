package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.commons.pojos.requests.OrganizationEntity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Setter
@Getter
public class AddToLibraryReq extends AbstractAuthCheckReq {
    @NotBlank
    public List<SrcEntity> contents;
    @NotBlank
    public List<OrganizationEntity> orgEntities;

    @NotBlank(message = "orgId should not be null")
    public String orgId;

    public String validate() {
        if (CollectionUtils.isNotEmpty(orgEntities)) {
            for (OrganizationEntity orgEntity : orgEntities) {
                String value = orgEntity.validate();
                if (value != null) {
                    return value;
                }
            }
        } else {
            return "orgentities missing";
        }
        if (CollectionUtils.isNotEmpty(contents)) {
            for (SrcEntity content : contents) {
              /*  String value = content.validate();
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
