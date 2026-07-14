package com.vedantu.content.managers;

import java.util.ArrayList;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.board.enums.BoardContextType;
import com.vedantu.board.managers.BoardManager;
import com.vedantu.board.pojos.requests.GetChildrenReq;
import com.vedantu.board.pojos.responses.GetChildrenRes;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.boards.BoardType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.SearchListResponse;
import com.vedantu.content.enums.search.SearchResultType;
import com.vedantu.content.pojos.requests.GetContentForDemoReq;
import com.vedantu.content.pojos.requests.GetContentReq;
import com.vedantu.content.pojos.requests.GetModuleReq;
import com.vedantu.content.pojos.requests.GetModulesReq;
import com.vedantu.content.pojos.requests.documents.GetDocumentsReq;
import com.vedantu.content.pojos.requests.tests.GetTestsReq;
import com.vedantu.content.pojos.responses.GetContentRes;
import com.vedantu.content.pojos.responses.GetModuleRes;
import com.vedantu.content.pojos.responses.documents.GetDocumentRes;
import com.vedantu.content.pojos.responses.tests.GetTestRes;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.managers.OrgMemberManager;
import com.vedantu.organization.managers.OrgProgramManager;
import com.vedantu.organization.models.OrgMember;
import com.vedantu.organization.pojos.OrgMemberMappingExtendedInfo;
import com.vedantu.organization.pojos.OrgProgramBasicInfo;
import com.vedantu.organization.pojos.requests.members.GetOrgMemberReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgProgramCoursesReq;
import com.vedantu.organization.pojos.responses.members.GetOrgMemberProfileRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgProgramCoursesRes;

public class ApplicationManager extends AbstractContentManager {

    private static final ALogger LOGGER = Logger.of(ApplicationManager.class);

    public static GetContentRes getContentResponse(GetContentReq request) throws VedantuException {
        GetContentRes response = new GetContentRes();

        OrgMember member = OrgMemberDAO.INSTANCE.getByUserId(request.userId);

        GetOrgMemberReq memberInfoReq = new GetOrgMemberReq(request.orgId, member.memberId);
        GetOrgMemberProfileRes memberInfo = OrgMemberManager.getOrgMemberByMemberId(memberInfoReq);
        response.memberInfo = memberInfo.info.mappings;

        OrgMemberMappingExtendedInfo mappings = response.memberInfo;
        if (mappings.programs != null && !mappings.programs.isEmpty()) {
            String programId = mappings.programs.get(0).id;
            String sectionId = mappings.programs.get(0).centers.get(0).sections.get(0).id;
            String centerId = mappings.programs.get(0).centers.get(0).id;
            if (request.programId != null) {
                programId = request.programId;
            }
            for (OrgProgramBasicInfo program : mappings.programs) {
                if (program.id.equals(programId)) {
                    sectionId = program.centers.get(0).sections.get(0).id;
                    centerId = program.centers.get(0).id;
                    break;
                }
            }
            response.programId = programId;
            if (request.parentId != null) {
                GetChildrenReq boardRequest = new GetChildrenReq();
                boardRequest.callingUserId = request.userId;
                boardRequest.userId = request.userId;
                boardRequest.callingApp = "learn-app";
                boardRequest.callingAppId = "learn-app";
                boardRequest.parentId = request.parentId;
                boardRequest.recordState = VedantuRecordState.ACTIVE;
                boardRequest.ownerId = request.orgId;
                boardRequest.showSharedSubjects = "show";
                boardRequest.type = BoardType.TOPIC;
                boardRequest.context = BoardContextType.ORG;
                GetChildrenRes boardResponse = BoardManager.getChildren(boardRequest);
                if (boardResponse.totalHits > 10) {
                    request.size = (int) boardResponse.totalHits + 1;
                }
                response.boardResponse = boardResponse;
            }
            GetOrgProgramCoursesReq programInfoReq = new GetOrgProgramCoursesReq(request.orgId,
                    programId);
            GetOrgProgramCoursesRes programInfo = OrgProgramManager
                    .getProgramCourses(programInfoReq);
            response.programInfo = programInfo;

            GetModulesReq moduleReq = new GetModulesReq();
            if (request.brdIds != null && !request.brdIds.isEmpty()) {
                moduleReq.brdIds = request.brdIds;
            }
            if(request.start != null){
                moduleReq.start = request.start;
            }
            if(request.size != null){
                moduleReq.size = request.size;
            }
            moduleReq.allBrds = false;
            moduleReq.facet = false;
            moduleReq.orderBy = "timeCreated";
            moduleReq.orgId = request.orgId;
            moduleReq.programId = programId;
            moduleReq.sectionId = sectionId;
            moduleReq.centerId = centerId;
            moduleReq.sortOrder = "DESC";
            moduleReq.resultType = SearchResultType.ALL;
            moduleReq.contentSrc = new SrcEntity(EntityType.ORGANIZATION, request.orgId);
            moduleReq.userId = request.userId;
            moduleReq.targetUserId = request.userId;
            SearchListResponse<GetModuleRes> moduleRes = ModuleManager.INSTANCE
                    .getModules(moduleReq);
            response.moduleRes = moduleRes;

            if(request.keepModuleResult){
                if(moduleRes.list != null && !moduleRes.list.isEmpty()){
                    String moduleId = moduleRes.list.get(0).id;
                    GetModuleReq getModuleRequest = new GetModuleReq();
                    getModuleRequest.id = moduleId;
                    getModuleRequest.orgId = request.orgId;
                    getModuleRequest.callingUserId = request.userId;
                    getModuleRequest.userId = request.userId;
                    getModuleRequest.callingApp = "learn-app";
                    getModuleRequest.callingAppId = "learn-app";
                    GetModuleRes moduleResult = ModuleManager.INSTANCE.getModule(getModuleRequest);
                    response.moduleResult = moduleResult;
                }
            }
            GetTestsReq testReq = new GetTestsReq();
            if (request.brdIds != null && !request.brdIds.isEmpty()) {
                testReq.brdIds = request.brdIds;
            }
            if(request.start != null){
                testReq.start = request.start;
            }
            if(request.size != null){
                testReq.size = request.size;
            }
            testReq.allBrds = false;
            testReq.facet = false;
            testReq.orderBy = "timeCreated";
            testReq.orgId = request.orgId;
            testReq.programId = programId;
            testReq.sectionId = sectionId;
            testReq.centerId = centerId;
            testReq.sortOrder = "DESC";
            testReq.resultType = SearchResultType.ALL;
            testReq.contentSrc = new SrcEntity(EntityType.ORGANIZATION, request.orgId);
            testReq.userId = request.userId;
            testReq.targetUserId = request.userId;
            testReq.published = true;
            SearchListResponse<GetTestRes> testRes = TestManager.getTests(testReq);
            response.testRes = testRes;

            GetDocumentsReq documentsReq = new GetDocumentsReq();
            if (request.brdIds != null && !request.brdIds.isEmpty()) {
                documentsReq.brdIds = request.brdIds;
            }
            if(request.start != null){
                documentsReq.start = request.start;
            }
            if(request.size != null){
                documentsReq.size = request.size;
            }
            documentsReq.allBrds = false;
            documentsReq.facet = false;
            documentsReq.orderBy = "timeCreated";
            documentsReq.orgId = request.orgId;
            documentsReq.programId = programId;
            documentsReq.sectionId = sectionId;
            documentsReq.centerId = centerId;
            documentsReq.sortOrder = "DESC";
            documentsReq.resultType = SearchResultType.ALL;
            documentsReq.contentSrc = new SrcEntity(EntityType.ORGANIZATION, request.orgId);
            documentsReq.userId = request.userId;
            documentsReq.targetUserId = request.userId;
            SearchListResponse<GetDocumentRes> documentsRes = DocumentManager.INSTANCE
                    .gets(documentsReq);
            response.documentRes = documentsRes;
        }
        return response;
    }

    public static GetContentRes getContentForDemo(GetContentForDemoReq request)
            throws VedantuException {
        GetContentReq req = new GetContentReq();
        req.orgId = "5513a38de4b095b85aa162fd";
        req.userId = "562dc3f5e4b04529cf8de996";
        req.keepModuleResult = true;
        req.brdIds = new ArrayList<String>();
        if (request.type.equalsIgnoreCase("JEE")) {
            req.programId = "5551f984e4b051e1254d915a";
        }
        if (request.type.equalsIgnoreCase("NEET")) {
            req.programId = "5564245ce4b097d3a9f1e436";
        }
        if (request.parentId.equals("5513d522e4b0a12d7940631e")) { // Physics 11
            req.brdIds.add("5513d522e4b0a12d794062c8");
        }
        if (request.parentId.equals("5513d522e4b0a12d794062fb")) { // chem11
            req.brdIds.add("5513d522e4b0a12d794062ef");
        }
        if (request.parentId.equals("5513d522e4b0a12d794062b5")) { // math11
            req.brdIds.add("5513d522e4b0a12d794062bb");
        }
        if (request.parentId.equals("5513d522e4b0a12d794062d1")) { // phy12
            req.brdIds.add("5513d522e4b0a12d7940629d");
        }
        if (request.parentId.equals("5513d522e4b0a12d794062e1")) { // chem12
            req.brdIds.add("5513d522e4b0a12d79406309");
        }
        if (request.parentId.equals("5513d522e4b0a12d794062d0")) { // math12
            req.brdIds.add("5513d522e4b0a12d794062e8");
        }
        if (request.parentId.equals("5513d522e4b0a12d794062e4")) { // botany 11
            req.brdIds.add("5513d522e4b0a12d794062bf");
        }
        if (request.parentId.equals("5513d522e4b0a12d794062e9")) { // zoology11
            req.brdIds.add("5513d522e4b0a12d794062a4");
        }
        if (request.parentId.equals("5513d522e4b0a12d794062e0")) { // botany12
            req.brdIds.add("5513d522e4b0a12d794062c6");
        }
        if (request.parentId.equals("5513d522e4b0a12d794062d5")) { // zoology12
            req.brdIds.add("5513d522e4b0a12d794062c1");
        }

        return getContentResponse(req);
    }

}
