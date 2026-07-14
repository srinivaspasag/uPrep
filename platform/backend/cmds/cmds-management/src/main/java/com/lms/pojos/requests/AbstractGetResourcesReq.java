package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.enums.EntityType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public abstract class AbstractGetResourcesReq extends AbstractAuthCheckReq {

    public List<String> brdIds;
    public String query;
    public List<EntityType> includes;
    public List<EntityType> excludes;
    public String includeTypes;
    public String includeDifficulty;
    public String orderBy;
    public String sortOrder = "desc";

    @NotNull
    public int start;
    public int size;
    @NotBlank(message = "orgId should not be empty")
    public String orgId;

    public AbstractGetResourcesReq() {

        super();
    }

    public AbstractGetResourcesReq(String callingUserId, String userId) {

        super(callingUserId, userId);
    }


}