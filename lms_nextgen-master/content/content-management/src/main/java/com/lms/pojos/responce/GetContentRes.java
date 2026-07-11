package com.lms.pojos.responce;

import com.lms.board.pojos.responces.GetChildrenRes;
import com.lms.pojo.OrgMemberMappingExtendedInfo;
import com.lms.pojo.responce.GetOrgProgramCoursesRes;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetContentRes {
    public OrgMemberMappingExtendedInfo memberInfo;
    public GetOrgProgramCoursesRes programInfo;
    public SearchListResponse<GetModuleRes> moduleRes;
    public SearchListResponse<GetTestRes> testRes;
    public SearchListResponse<GetDocumentRes> documentRes;
    public GetChildrenRes boardResponse;
    public String programId;
    public GetModuleRes moduleResult;
}
