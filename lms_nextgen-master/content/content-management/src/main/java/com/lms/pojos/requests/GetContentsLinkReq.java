package com.lms.pojos.requests;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.lms.common.vedantu.commons.pojos.requests.AbstractOrgListReq;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.UserActionType;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class GetContentsLinkReq extends AbstractOrgListReq {

	@NotBlank(message = "userid should not be empty")
    public String targetUserId;

    @NotNull
    public SrcEntity target;

    @NotNull
    public UserActionType linkType;

    public long addedAfter;

    public boolean addContent;
}
