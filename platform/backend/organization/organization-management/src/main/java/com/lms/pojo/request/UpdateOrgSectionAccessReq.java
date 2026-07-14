package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class UpdateOrgSectionAccessReq {

    public List<OrgSectionAccessInfo> sectionAccessInfos;
}
