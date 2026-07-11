package com.lms.pojo.request;

import com.lms.models.AddOrgMemberMappingReq;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class UpdateOrgMemberMappingReq extends AddOrgMemberMappingReq {

    public List<String> removeCourseIds;

}
