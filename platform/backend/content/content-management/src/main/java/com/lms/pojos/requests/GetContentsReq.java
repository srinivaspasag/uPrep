package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractOrgListReq;
import com.lms.common.vedantu.enums.EntityType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Setter
@Getter
public class GetContentsReq extends AbstractOrgListReq {
    @NotBlank(message = "targetUserId should not be null")
    public String targetUserId;
    @NotBlank(message = "ids should not be null")
    public List<String> ids;

    @NotBlank(message = "type should not be null")
    public EntityType type;
    public boolean addAnswer;
}
