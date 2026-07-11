package com.lms.pojos.requests;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.lms.common.vedantu.entity.storage.MediaType;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.pojo.request.AbstractOrgScopeReq;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class GetContentSecuredDownloadLinkReq extends AbstractOrgScopeReq {
    @NotNull
    public EntityType entityType;
    
    @NotNull
    public MediaType mediaType;

    @NotBlank(message = "entityId should not be empty")
    public String     entityId;

    @NotBlank(message = "fileName should not be empty")
    public String     fileName;

}
