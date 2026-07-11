package com.lms.pojo.request;

import java.util.List;

import javax.validation.constraints.NotBlank;

import com.lms.common.vedantu.commons.pojos.requests.InputFieldInfo;
import com.lms.enums.OrgMemberProfile;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class UpdateOrgMemberExtraInfoInputFieldsReq extends AbstractOrgScopeReq {
	
public OrgMemberProfile     targetOrgMemberProfile;
public List<InputFieldInfo> fields;

}
