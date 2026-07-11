package com.vedantu.content.pojos.responses;

import com.vedantu.board.pojos.responses.GetChildrenRes;
import com.vedantu.commons.pojos.responses.SearchListResponse;
import com.vedantu.content.pojos.responses.documents.GetDocumentRes;
import com.vedantu.content.pojos.responses.tests.GetTestRes;
import com.vedantu.organization.pojos.OrgMemberMappingExtendedInfo;
import com.vedantu.organization.pojos.responses.organizations.GetOrgProgramCoursesRes;

public class GetContentRes {

    public OrgMemberMappingExtendedInfo       memberInfo;
    public GetOrgProgramCoursesRes            programInfo;
    public SearchListResponse<GetModuleRes>   moduleRes;
    public SearchListResponse<GetTestRes>     testRes;
    public SearchListResponse<GetDocumentRes> documentRes;
    public GetChildrenRes                     boardResponse;
    public String                             programId;
    public GetModuleRes                       moduleResult;
}
