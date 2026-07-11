package com.lms.services.serviceImpl;

import com.lms.board.components.BoardManager;
import com.lms.board.enums.BoardContextType;
import com.lms.board.model.Board;
import com.lms.board.model.GranteeOrgProgram;
import com.lms.board.pojos.requests.GetChildrenReq;
import com.lms.board.pojos.responces.GetChildrenRes;
import com.lms.board.pojos.test.BoardBasicInfo;
import com.lms.board.repo.BoardRepo;
import com.lms.board.repo.GranteeOrgProgramRepo;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.pojos.DownloadableFileInfo;
import com.lms.common.utils.EncryptionUtils;
import com.lms.common.utils.ImageHTMLUtils;
import com.lms.common.vedantu.commons.pojos.requests.SecurityCredentials;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.enums.BoardType;
import com.lms.common.vedantu.enums.EncryptionLevel;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.components.ContentComponent;
import com.lms.components.ModuleComponent;
import com.lms.components.QuestionComponent;
import com.lms.enums.QuestionType;
import com.lms.enums.SearchResultType;
import com.lms.managers.AbstractTestManager;
import com.lms.managers.ContentSecurityManager;
import com.lms.models.Module;
import com.lms.models.*;
import com.lms.models.tests.Assignment;
import com.lms.organization.auth.AuthHandler;
import com.lms.organization.auth.AuthHandlerFactory;
import com.lms.pojo.*;
import com.lms.pojo.request.GetOrgMemberReq;
import com.lms.pojo.request.GetOrgProgramCoursesReq;
import com.lms.pojo.responce.GetOrgMemberProfileRes;
import com.lms.pojo.responce.GetOrgProgramCoursesRes;
import com.lms.pojos.requests.*;
import com.lms.pojos.responce.*;
import com.lms.pojos.responce.questions.GetSolutionsRes;
import com.lms.repository.*;
import com.lms.services.ContentService;
import com.lms.user.vedantu.user.model.User;
import com.lms.user.vedantu.user.pojo.UserExtendedInfo;
import com.lms.user.vedantu.user.repository.UserRepo;
import com.mongodb.BasicDBObject;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.internal.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ContentServiceImpl extends AbstractTestManager implements ContentService {
    private static final Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ContentSecurityManager contentSecurityManager;
    @Autowired
    private ModuleComponent moduleComponent;
    @Autowired
    private ContentComponent contentComponent;
    private OrganizationRepo organizationRepo;
    @Autowired
    private OrgMemberRepo orgMemberRepo;
    @Autowired
    private GranteeOrgProgramRepo granteeOrgProgramRepo;
    @Autowired
    private BoardRepo boardRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private OrgProgramRepo orgProgramRepo;
    @Autowired
    private OrgCenterRepo orgCenterRepo;
    @Autowired
    private OrgSectionRepo orgSectionRepo;
    @Autowired
    private AnswerRepo answerRepo;
    @Autowired
    private QuestionRepo questionRepo;
    @Autowired
    private QuestionComponent questionComponent;
    @Autowired
    private ModuleRepo moduleRepo;
    @Autowired
    private ModuleServiceImpl moduleService;
    @Autowired
    private TestServiceImpl testService;
    @Autowired
    private DocumentsServiceImpl documentsService;
    @Autowired
    private BoardManager boardManager;
    @Autowired
    private AssignmentRepo assignmentRepo;
    @Autowired
    private DocumentsRepo documentsRepo;
    @Autowired
    private TestRepo testRepo;

    @Override
    public VedantuResponse getContentLinks(GetContentsLinkReq getContentsLinkReq) {
        GetContentLinksRes getContentLinksRes = null;
        try {
            getContentLinksRes = getContentLinksRes(getContentsLinkReq);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(getContentLinksRes);
    }

    @Override
    public VedantuResponse getContentresponse(GetContentReq request) {
        if (request == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        GetContentRes getContentRes = getContentResponse(request);
        return new VedantuResponse(getContentRes);
    }

    public  GetOrgMemberProfileRes getOrgMemberByMemberId(GetOrgMemberReq req)
            throws VedantuException {

        OrgMember orgMember = getMemberByMemberId(req.orgId, req.memberId);
        if (null == orgMember) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND,
                    "No member found with memberId : " + req.memberId);
        }

        GetOrgMemberProfileRes getOrgMemberProfileRes = getOrgMemberProfileRes(orgMember,req.ensureCourseInfo, req.getKey);

        return getOrgMemberProfileRes;

    }
    private GetOrgMemberProfileRes getOrgMemberProfileRes(OrgMember orgMember, boolean ensureCourseInfo, boolean addKey)
            throws VedantuException {

        OrgMemberExtendedInfo orgMemberExtendedInfo = (OrgMemberExtendedInfo) orgMember.toExtendedInfo();
        populateUserPublicProfileDetails(orgMember, orgMemberExtendedInfo);

        populateProgramHierarchy(orgMember, orgMemberExtendedInfo, ensureCourseInfo);

        GetOrgMemberProfileRes getOrgMemberProfileRes = new GetOrgMemberProfileRes();
        Optional<Organization> org = organizationRepo.findById(orgMember.getOrgId());
        getOrgMemberProfileRes.setInfo(orgMemberExtendedInfo);
        getOrgMemberProfileRes.setDoubtsForumMode(org.get().getDoubtsForumMode());
        getOrgMemberProfileRes.setShowClassroomConnect(org.get().isShowClassroomConnect());
        if (addKey) {
            getOrgMemberProfileRes.key = getPrivateKey(orgMember.getUserId());
        }
        return getOrgMemberProfileRes;
    }
    private void populateUserPublicProfileDetails(OrgMember orgMember,
                                                  OrgMemberExtendedInfo orgMemberExtendedInfo) throws VedantuException {

        UserExtendedInfo userExtendedInfo = getExtendedInfo(orgMember.getUserId());
        if (null == userExtendedInfo) {
            logger.debug("populateUserPublicProfileDetails no userExtendedInfo found for userId: "
                    + orgMember.userId);
            return;
        }
        final String username = userExtendedInfo.getUsername();
        final String verifiedEmail = userExtendedInfo.isEmailVerified ? userExtendedInfo.getEmail()
                : HardCodedConstants.emptyString;
        String s = getAuthHandler(orgMember.getOrgId())
                .getMemberUsername(orgMember.getOrgId(), orgMember.getMemberId());
        final boolean isUsernameOrgSpecific = username.equals(s);

        orgMemberExtendedInfo.setUserPublicProfileDetails(username, verifiedEmail,
                isUsernameOrgSpecific);

        // if the no joinedTime is populated in the org member mapping so update
        // it as the time of
        // user creation
        boolean updatedMapping = false;

        if (orgMember.getMappings() != null) {
            for (OrgMemberMappingInfo mapping : orgMember.getMappings()) {
                if (mapping.getTimeJoined() < 1) {
                    mapping.setTimeJoined(orgMember.getTimeCreated());
                    updatedMapping = true;
                }
            }
        }

        if (updatedMapping) {
            orgMemberRepo.save(orgMember);
        }

    }
    private UserExtendedInfo getExtendedInfo(String userId) {
        Optional<User> user = userRepo.findById(userId);
        if (!user.isPresent())
            return null;
        UserExtendedInfo extendedInfo = new UserExtendedInfo(user.get());
        return extendedInfo;
    }
    public AuthHandler getAuthHandler(String orgId) throws VedantuException {

        Optional<Organization> organization = organizationRepo.findById(orgId.trim());
        if (!organization.isPresent()) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND,
                    "no org found with id: " + orgId);
        }

        AuthHandler authHandler = AuthHandlerFactory.getInstance().getAuthHandler(organization.get());
        return authHandler;

    }


    public OrgMember getMemberByMemberId(String orgId, String memberId) {

        logger.debug("getMemberByMemberId orgId: " + orgId + ", memberId: " + memberId);


        OrgMember orgMember = orgMemberRepo.findByOrgIdAndMemberIdAndStatus(orgId,memberId.toUpperCase(),OrgMember.FIELD_STATUS);


        if (null == orgMember) {
            logger.error("cannot find orgMember for orgId: " + orgId + ", memberId: " + memberId);
        }

        logger.info("getMemberByMemberId found orgMember: " + orgMember);

        return orgMember;
    }

    private GetContentRes getContentResponse(GetContentReq request) {


       GetContentRes response = new GetContentRes();

            OrgMember member = orgMemberRepo.findByUserId(request.getUserId());

            GetOrgMemberReq memberInfoReq = new GetOrgMemberReq(request.orgId, member.memberId);
            GetOrgMemberProfileRes memberInfo = getOrgMemberByMemberId(memberInfoReq);
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
                    GetChildrenRes boardResponse = boardManager.getChildren(boardRequest);
                    if (boardResponse.totalHits > 10) {
                        request.size = (int) boardResponse.totalHits + 1;
                    }
                    response.boardResponse = boardResponse;
                }
                GetOrgProgramCoursesReq programInfoReq = new GetOrgProgramCoursesReq(request.orgId,
                        programId);
                GetOrgProgramCoursesRes programInfo = getProgramCourses(programInfoReq);
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
                SearchListResponse<GetModuleRes> moduleRes = moduleService.getModulesForContentResponce(moduleReq);
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
                        //Todo need to implement
                        GetModuleRes moduleResult = moduleService.getModuleRes(getModuleRequest);
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
                SearchListResponse<GetTestRes> testRes = testService.getTestsForContentResponce(testReq);
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
                SearchListResponse<GetDocumentRes> documentsRes = documentsService.gets(documentsReq);
                response.documentRes = documentsRes;
            }
            return response;

    }


    public GetChildrenRes getChildren(GetChildrenReq getChildrenReq) throws VedantuException {
        if (null == getChildrenReq) {
            String errorMsg = "null getChildrenReq";
            logger.error("getChildren " + errorMsg);
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, errorMsg);
        }
        String validation = getChildrenReq.validate();
        if (!StringUtils.isEmpty(validation)) {
            logger.error(validation);
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, validation);
        }
        Set<String> grantedOrgsIds = new LinkedHashSet<String>();
        //Get all the orgIds that gave access to the current organization
        if (getChildrenReq.showSharedSubjects != null && getChildrenReq.showSharedSubjects != "" && getChildrenReq.showSharedSubjects.equals("show")){
            AtomicLong totalProgramHits = new AtomicLong(0L);
            List<GranteeOrgProgram> granteeOrgPrograms = getGranteeOrgPrograms(getChildrenReq.ownerId, null, totalProgramHits);
            for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
                grantedOrgsIds.add(granteeOrgProgram.providerOrgId);
            }
        }
        Board brd = null;
        if(!StringUtils.isEmpty(getChildrenReq.parentId)){
           Optional<Board> brd1 = boardRepo.findById(getChildrenReq.getParentId());
                  brd=brd1.get();
        }
        //       // Adding the current organization ID
        grantedOrgsIds.add(getChildrenReq.ownerId);
        Set<Board> boards = new LinkedHashSet<Board>();
        for (String orgId : grantedOrgsIds) {
            if (brd != null && !brd.ownerId.equals(orgId)) {
                orgId = brd.ownerId;
            }
            boards.addAll(getBoardBasicInfos(getChildrenReq.context, orgId,
                    getChildrenReq.type, getChildrenReq.parentId, getChildrenReq.recordState));
        }
        List<Board> boardsList = new ArrayList<Board>();
        boardsList.addAll(boards);
        List<BoardBasicInfo> boardBasicInfos = null;//BoardDAO.INSTANCE.toBasicInfos(boardsList);
        GetChildrenRes getChildrenRes = new GetChildrenRes();
        getChildrenRes.list = boardBasicInfos;
        getChildrenRes.totalHits = boardBasicInfos.size();

        logger.info("getChildren list.size" + CollectionUtils.size(getChildrenRes.list));

        return getChildrenRes;

    }

    public List<Board> getBoardBasicInfos(BoardContextType context, String ownerId, BoardType type,
                                          String parentId) throws VedantuException {

        return getBoardBasicInfos(context, ownerId, type, parentId, VedantuRecordState.ACTIVE);
    }

    public List<Board> getBoardBasicInfos(BoardContextType context, String ownerId, BoardType type,
                                          String parentId, VedantuRecordState state) throws VedantuException {

        logger.debug("getBoardBasicInfos context: " + context + ", ownerId: " + ownerId
                + ", type: " + type + ", parentId: " + parentId);


        Criteria criteria = new Criteria();
        Query query = new Query();
        if (!StringUtils.isEmpty(ownerId)) {
            criteria.and("ownerId").is(ownerId);
        }
        if (!StringUtils.isEmpty(parentId)) {
            criteria.and("parentBrdIds").is(parentId);

        } else {
            // give root nodes for GLOBAL context
            if (BoardContextType.GLOBAL == context) {
                criteria.and("parentBrdIds").is(null);
            }
        }
        criteria.and("context").is(context.name());

        if (null != type) {
            criteria.and("type").is(type.name());
        }

        if (state != null) {
            criteria.and("recordState").is(state.name());
        }
        query.addCriteria(criteria);
        List<Board> bords = mongoTemplate.find(query, Board.class);


        if (null != bords) {
            return bords;
        } else {
            logger.info("getBoardBasicInfos no boards found");
            return new ArrayList<Board>();
        }

    }
    public GetOrgProgramCoursesRes getProgramCourses(
            GetOrgProgramCoursesReq getOrgProgramCoursesReq)
            throws VedantuException {

        //Get all the orgIds that gave access to the current organizations programs
        AtomicLong totalProgramHits = new AtomicLong(0L);
        List<GranteeOrgProgram> granteeOrgPrograms = getGranteeOrgPrograms(getOrgProgramCoursesReq.orgId, null, totalProgramHits);
        List<String> grantedOrgs = new ArrayList<String>();
        /*
         *
         * If programid is null
         * 	get  programcours with currentorg, programid
         * 	for each of grateeorgprogram of this org
         * 		clll getprogramcourses ( granteprogram.properorg, grateeprog.programid)
         *
         * IF NOT NULL{
         * 	BUILD ORGANIATION LIST AND PASS THIS TO
         * }
         */
        for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
            grantedOrgs.add(granteeOrgProgram.providerOrgId);
            logger.debug("......entering for loop getPrograCourses......" + grantedOrgs);
        }


        grantedOrgs.add(getOrgProgramCoursesReq.orgId);
        Set<BoardBasicInfo> courseBoardInfos = null;//OrgProgramDAO.INSTANCgetProgramCourses(grantedOrgs, getOrgProgramCoursesReq.programId);
        GetOrgProgramCoursesRes getOrgProgramCoursesRes = new GetOrgProgramCoursesRes();
        getOrgProgramCoursesRes.list = new ArrayList<BoardBasicInfo>(
                courseBoardInfos);
        Collections.sort(getOrgProgramCoursesRes.list, BoardBasicInfo.COMPARATOR);
        getOrgProgramCoursesRes.totalHits = CollectionUtils
                .size(courseBoardInfos);

        return getOrgProgramCoursesRes;
    }

    public List<GranteeOrgProgram> getGranteeOrgPrograms(String providerOrgId, String departmentId,
                                                         AtomicLong totalHits) {
        logger.debug("getGrateeOrgPrograms orgId: " + providerOrgId
                + ", departmentId: " + departmentId);
        List<GranteeOrgProgram> granteeOrgList = granteeOrgProgramRepo.findAllBySubscriberOrgIdAndRecordState(providerOrgId, VedantuRecordState.ACTIVE);

        totalHits.set(granteeOrgList.stream().count());
        return granteeOrgList;
    }

    public GetContentLinksRes getContentLinksRes(GetContentsLinkReq req)
            throws VedantuException {

        return getContentLinks(req, VedantuRecordState.ACTIVE);
    }

    private GetContentLinksRes getContentLinks(GetContentsLinkReq req, VedantuRecordState recordState) {
        GetContentLinksRes res = new GetContentLinksRes();
        Set<String> childrenIds = null;
        //To find and eliminate useless contentlinks with target.id as moduleId - part 1
        if (req.target.type == EntityType.MODULE) {
            Module module = moduleComponent.getModuleById(req.target.id);
            if (module == null) {
                throw new VedantuException(VedantuErrorCode.INVALID_ID);
            }
            childrenIds = new HashSet<String>(module.children.size());
            for (ModuleEntry child : module.children) {
                if (child.entity == null) {
                    continue;
                }
                childrenIds.add(child.entity.id);
            }
            logger.debug("Bosa debug log ::childrenIds: " + childrenIds);
        }
        //End - To find and eliminate useless contentlinks with target.id as moduleId - part 1


        Criteria criteria = new Criteria();
        Query query1 = new Query();
        criteria.and(ConstantsGlobal.TARGET_DOT_TYPE).is(req.target.type.name());
        criteria.and(ConstantsGlobal.TARGET_DOT_ID).is(req.target.id);
        criteria.and(ConstantsGlobal.LINK_TYPE).is(req.linkType.name());
        if (recordState == VedantuRecordState.ACTIVE) {
            criteria.and("scope").ne(Scope.PRIVATE.name());
        }
        criteria.and(ConstantsGlobal.RECORD_STATE).is(recordState.name());
        if (req.addedAfter > 0) {
            criteria.gt(new BasicDBObject(ConstantsGlobal.LAST_UPDATED, req.addedAfter));
        }
        query1.addCriteria(criteria);
        query1.skip(req.start);
        query1.limit(req.size);
        List<LibraryContentLink> links = mongoTemplate.find(query1, LibraryContentLink.class);

        res.totalHits = links.size();

        //To find and eliminate useless contentlinks with target.id as moduleId - part 2
        /*if (childrenIds != null) {
            Iterator<LibraryContentLink> linkIterator = links.results.iterator();
            while(linkIterator.hasNext()) {
                LibraryContentLink libraryContentLink = linkIterator.next();
                if(!childrenIds.contains(libraryContentLink.source.id)) {
                    LOGGER.debug("Bosa debug log ::removing: " + libraryContentLink.source.id);
                    linkIterator.remove();
                    res.totalHits--;
                }
            }
        }*/
        if (childrenIds != null) {
            for (LibraryContentLink contentLink : links) {
                if (!childrenIds.contains(contentLink.source.id)) {
                    logger.debug("Bosa debug log ::removing: " + contentLink.source.id);
                    links.remove(contentLink);
                }
            }
        }
        //End - To find and eliminate useless contentlinks with target.id as moduleId - part 2

       // Map<SrcEntity, ContentSearchDetails> entityDetailsMap = null;
        if (req.addContent) {
           // entityDetailsMap = getEntityDetailsMap(req.orgId, req.targetUserId, links.results);
        }

        //ContentSecurityManager csManager = new ContentSecurityManager();


        for (LibraryContentLink link : links) {
            GetContentLinkRes cLink = new GetContentLinkRes(link._getStringId(), link.timeCreated,
                    link.lastUpdated, link.target, link.isDownloadable(),
                    link.getDownloadableEntities(), (int) link.position);
            if (req.addContent) {
               // cLink.content = entityDetailsMap.get(link.source);
            } else if (recordState == VedantuRecordState.DELETED) {
                // only in case of deleted link the link source info will be returned not the
                // complete ContentSearchDetails object
                cLink.content = link.source;
            }
            EncryptionLevel encLevel = contentSecurityManager.getEncLevel(link, req.orgId);
            boolean encrypted = encLevel != null && encLevel != EncryptionLevel.NA;
            EntityType srcType = link.source.type;
            if(srcType == EntityType.TEST){
                logger.debug("******** getContentLinks Test id is "+link.source.id);
            }
//            if(link.getSchedule() != null){
//                try {
//                    JSONObject temp = new JSONObject(((ContentSearchDetails)cLink.content).getInfo());
//                    temp.put("startTime", link.getSchedule().startTime.getTime());
//                    ((ContentSearchDetails)cLink.content).setInfo(temp.toString());
//                } catch (JSONException e) {
//                    LOGGER.debug("******** getContentLinks Exception came "+e.getMessage());
//                }
//            }
            if(link.getSchedule() != null){
                if(link.getSchedule().startTime != null){
                    cLink.startTime = link.getSchedule().startTime.getTime();
                }else{
                    cLink.startTime = Long.MIN_VALUE;
                }
                if(link.getSchedule().endTime != null) {
                    cLink.endTime = link.getSchedule().endTime.getTime();
                }else{
                    cLink.endTime = Long.MIN_VALUE;
                }
                if(link.getSchedule().closeTime != null) {
                    cLink.closeTime = link.getSchedule().closeTime.getTime();
                }else{
                    cLink.closeTime = Long.MIN_VALUE;
                }
            }
            if (encrypted && srcType != EntityType.QUESTION && srcType != EntityType.TEST
                    && srcType != EntityType.ASSIGNMENT && srcType != EntityType.MODULE) {
                cLink.encLevel = encLevel;
                cLink.passphrase = contentSecurityManager.getPassphrase(encLevel, link, req.userId, req.orgId);
            }

            res.list.add(cLink);
        }
        if (res.list.size() > 0) {
            res.latestContent = res.list.get(res.list.size() - 1).lastUpdated;
        }
        else {
            res.latestContent = req.addedAfter;
        }
        
        return res;
    }
        	


	public EncryptionLevel getEncLevel(LibraryContentLink contentLink, String orgId)
            throws VedantuException {

        if (contentLink == null) {
            throw new VedantuException(VedantuErrorCode.ACCESS_DENIED, "invalid content link");
        }

        Optional<Organization> organizationOptional = organizationRepo.findById(orgId);
        if (!organizationOptional.isPresent()) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND,
                    "organization not found");
        }
        Organization organization = organizationOptional.get();

        EncryptionLevel calculatedEncLevel = (contentLink.getEncLevel() == null || contentLink
                .getEncLevel() == EncryptionLevel.NA) ? organization.encLevel : contentLink
                .getEncLevel();
        return calculatedEncLevel;
    }

    @Override
    public VedantuResponse getRemovedContentLinks(GetContentsLinkReq getContentsLinkReq) {
        ListResponse<GetContentLinkRes> getRemovedContentLinksRes = null;
        try {
            getRemovedContentLinksRes =
                    getRemovedContentLinksRes(getContentsLinkReq);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(getRemovedContentLinksRes);
    }

    @Override
    public VedantuResponse getContentForDemo(GetContentForDemoReq request) {
        if (request==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetContentRes getContentRes = getContentFordemo(request);
        return new VedantuResponse(getContentRes);    }

    @Override
    public VedantuResponse getcontents(GetContentsReq getContentsReq) {
        if (getContentsReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        ListResponse<ContentSearchDetails> getContentsRes = getContents(getContentsReq);
        return new VedantuResponse(getContentsRes);
    }
    public  ListResponse<ContentSearchDetails> getContents(GetContentsReq req) {

        Query esQuery = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ConstantsGlobal.ID).in(req.ids.toArray());
        criteria.and(ConstantsGlobal.TYPE).is(req.type.name().toLowerCase());
        esQuery.addCriteria(criteria);
        ListResponse<ContentSearchDetails> searchList = null;
        List<ContentSearchDetails> lsd = null;
        if (req.getType() == EntityType.MODULE) {
            List<Module> modules = moduleRepo.findByIdInAndContentType(req.getIds().toArray(), req.getType());
            for (Module module : modules) {
                ContentSearchDetails conSDetails = new ContentSearchDetails();
                conSDetails.setType(module.getContentType());
                conSDetails.setRecordState(module.getRecordState());
                conSDetails.userId = module.getUserId();
                conSDetails.setId(module.getUserId());
                lsd.add(conSDetails);

            }
        } else if (req.getType() == EntityType.QUESTION) {
            List<Question> questions = questionRepo.findByIdInAndContentType(req.getIds().toArray(), req.getType());
            for (Question module : questions) {
                ContentSearchDetails conSDetails = new ContentSearchDetails();
                conSDetails.setType(module.getContentType());
                conSDetails.setRecordState(module.getRecordState());
                conSDetails.userId = module.getUserId();
                conSDetails.setId(module.getUserId());
                lsd.add(conSDetails);

            }

        } else if (req.getType() == EntityType.ASSIGNMENT) {
            List<Assignment> assignments = assignmentRepo.findByIdInAndContentType(req.getIds().toArray(), req.getType());
            for (Assignment module : assignments) {
                ContentSearchDetails conSDetails = new ContentSearchDetails();
                conSDetails.setType(module.getContentType());
                conSDetails.setRecordState(module.getRecordState());
                conSDetails.userId = module.getUserId();
                conSDetails.setId(module.getUserId());
                lsd.add(conSDetails);

            }
        } else if (req.getType() == EntityType.DOCUMENT) {
            List<Documents> documents = documentsRepo.findByIdInAndContentType(req.getIds().toArray(), req.getType());
            for (Documents module : documents) {
                ContentSearchDetails conSDetails = new ContentSearchDetails();
                conSDetails.setType(module.getContentType());
                conSDetails.setRecordState(module.getRecordState());
                conSDetails.userId = module.getUserId();
                conSDetails.setId(module.getUserId());
                lsd.add(conSDetails);

            }
        } else if (req.getType() == EntityType.TEST) {
            List<Test> tests = testRepo.findByIdInAndContentType(req.getIds().toArray(), req.getType());
            for (Test module : tests) {
                ContentSearchDetails conSDetails = new ContentSearchDetails();
                conSDetails.setType(module.getContentType());
                conSDetails.setRecordState(module.getRecordState());
                conSDetails.userId = module.getUserId();
                conSDetails.setId(module.getUserId());
                lsd.add(conSDetails);

            }
        }


        searchList.setList(lsd);

        annotateExtraInfo(HardCodedConstants.emptyString, req.userId, req.orgId, req.type, searchList.list, true);
        if (req.type == EntityType.TEST) {
            logger.debug("**************   getContents    Test id is " + req.ids);
        }
        if (req.type == EntityType.QUESTION) {
            addParagraphInfo(req.ids, searchList);
            if (req.addAnswer) {
                annotateQuestionAnswerInfo(req.ids, searchList);
            }
        }
        return searchList;
    }

    private void annotateQuestionAnswerInfo(Collection<String> ids,
                                            ListResponse<ContentSearchDetails> searchList) {

        Map<String, Answer> ansMap = getQuestionAnswerMap(ids);
        for (ContentSearchDetails sDetails : searchList.list) {
            Answer ans = ansMap.get(sDetails.id);
            GetSolutionsReq solutionsReq = new GetSolutionsReq();
            solutionsReq.qId = sDetails.id;
            GetSolutionsRes solutionRes;
            try {
                solutionRes = questionComponent.getSolutions(solutionsReq);
                annotateQuestionAnswerInfo(ans, solutionRes, sDetails);
            } catch (VedantuException e) {
                logger.error(e.getMessage(), e);
            }

        }
    }

    public Map<String, Answer> getQuestionAnswerMap(Collection<String> qIds) {
        Map<String, Answer> answerMap = new HashMap<String, Answer>();
        //  Query query = getQuery().field(ConstantsGlobal.QID).in(qIds);
        List<Answer> answers = answerRepo.findByqIdIn(qIds);
        for (Answer ans : answers) {
            answerMap.put(ans.qId, ans);
        }
        for (String qId : qIds) {
            if (!answerMap.containsKey(qId)) {
                Optional<Question> question = questionRepo.findById(qId);
                if (question.isPresent() && question.get().getAnswerId() != null && !question.get().getAnswerId().isEmpty()) {
                    Optional<Answer> answer = answerRepo.findById(question.get().getAnswerId());
                    answerMap.put(qId, answer.get());
                }
            }
        }
        return answerMap;
    }

    private GetContentRes getContentFordemo(GetContentForDemoReq request) {
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

    public GetContentLinksRes getRemovedContentLinksRes(GetContentsLinkReq req)
            throws VedantuException {

        req.addContent = false;
        return getContentLinks(req, VedantuRecordState.DELETED);
    }
    private void populateProgramHierarchy(OrgMember orgMember,
                                          OrgMemberExtendedInfo orgMemberExtendedInfo, boolean ensureCourseInfo) {

        if (orgMember.getMappings() != null) {

            for (OrgMemberMappingInfo mapping : orgMember.getMappings()) {
                if (mapping == null) {
                    continue;
                }

                OrgStructureBasicInfo program = getProgramBasicInfo(mapping.getProgramId());
                OrgProgramBasicInfo programInfo = null;
                if (program != null)
                    programInfo = orgMemberExtendedInfo.mappings
                            ._getOrAddProgram(program);

                if (ensureCourseInfo && program != null) {
                    mapping.courseIds = programInfo.courseIds;
                }

                OrgStructureBasicInfo progCenter = getCenterBasicInfo(mapping.centerId);
                OrgProgramCenterBasicInfo progCenterInfo = null;
                if (progCenter != null && programInfo != null)
                    progCenterInfo = programInfo._getOrAddProgramCenter(progCenter);

                Optional<OrgSection> orgSection = orgSectionRepo.findById(mapping.getSectionId());
                if (orgSection.isPresent()) {

                    OrgStructureBasicInfo progSection = (OrgStructureBasicInfo) orgSection.get().toBasicInfo();
                    if (progSection != null && progCenterInfo != null) {
                        OrgProgramSectionBasicInfo progSectionInfo = progCenterInfo
                                ._getOrAddProgramSection(progSection);

                        progSectionInfo.setOrderId(mapping.getOrderId());
                        progSectionInfo.setTimeJoined(mapping.timeJoined);
                        progSectionInfo.setEndTime(mapping.endTime);
                        if (ensureCourseInfo) {
                            // for now only add desc, if needed we can
                            // progSectionInfo.addSectionExtraInfo(orgSection);
                            progSectionInfo.desc = (orgSection.get().getDesc() != null) ? orgSection.get().getDesc() : HardCodedConstants.emptyString;

                            progSectionInfo.addSectionExtraInfo(orgSection.get());
                        }


                        if (!mapping.getCourseIds().isEmpty()) {
                            for (String courseId : mapping.getCourseIds()) {
                                if (StringUtils.isEmpty(courseId)) {
                                    continue;
                                }
                                BoardBasicInfo course = getBoardBasicInfo(courseId);
                                if (null != course) {
                                    progSectionInfo._getOrAddProgramCourse(course);
                                }
                            }
                        }
                    }
                }

            }
        }
    }
    private BoardBasicInfo getBoardBasicInfo(String courseId) {
        Optional<Board> board = boardRepo.findById(courseId);
        if (!board.isPresent())
            return null;
        BoardBasicInfo orgStructureBasicInfo = new BoardBasicInfo(board.get());
        return orgStructureBasicInfo;
    }

    private OrgStructureBasicInfo getProgramBasicInfo(String programId) {
        Optional<OrgProgram> org = orgProgramRepo.findById(programId);
        if (!org.isPresent())
            return null;
        OrgStructureBasicInfo orgStructureBasicInfo = new OrgStructureBasicInfo(org.get());
        return orgStructureBasicInfo;
    }

    private OrgStructureBasicInfo getCenterBasicInfo(String centerId) {
        Optional<OrgCenter> org = orgCenterRepo.findById(centerId);
        if (!org.isPresent())
            return null;
        OrgStructureBasicInfo orgStructureBasicInfo = new OrgStructureBasicInfo(org.get());
        return orgStructureBasicInfo;
    }

    public String getPrivateKey(String userId) throws VedantuException {

        Optional<User> user = userRepo.findById(userId);
        if (!user.isPresent()) {
            return null;
        }
        SecurityCredentials credentials = user.get().getCredentials();
        if (credentials == null) {
            credentials = setCredentials(user.get());
        }
        // DatatypeConverter.printHexBinary(credentials.getPrivateKey())
        return Base64.encode(credentials.getPrivateKey());
    }
    private synchronized SecurityCredentials setCredentials(User user)
            throws VedantuException {

        if (user.getCredentials() != null) {
            return user.getCredentials();

        }
        user.setCredentials(EncryptionUtils.generateKeys());
        userRepo.save(user);
        return user.getCredentials();
    }

	@Override
	public VedantuResponse getContentDownloadLink(GetContentDownloadLinkReq getContentDownloadLinkReq) {
		GetContentDownloadLinkRes getContentDownloadUrlRes = null;
        try {
            getContentDownloadUrlRes = contentComponent
                    .getContentDownloadLink(getContentDownloadLinkReq);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(getContentDownloadUrlRes);
	}

	@Override
	public VedantuResponse getPdfDownloadLink(GetDownloadUrlOfPdfReq getDownloadUrlOfPdfReq) {
		GetDownloadUrlOfPdfRes getPdfDownloadUrlRes = null;
        try {
            getPdfDownloadUrlRes = contentComponent
                    .getPdfUrl(getDownloadUrlOfPdfReq);
        } catch (VedantuException e) {
           throw e;
        }
        return new VedantuResponse(getPdfDownloadUrlRes);
	}

	@Override
	public VedantuResponse getSecureLink(GetContentSecuredDownloadLinkReq getContentSecuredDownloadLinkReq) {
	    DownloadableFileInfo getContentDownloadUrlRes = null;
        try {
            getContentDownloadUrlRes = contentComponent.getSecureLink(getContentSecuredDownloadLinkReq);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(getContentDownloadUrlRes);
	}

	@Override
	public VedantuResponse getEntityInfoForApp(GetEntityInfoForAppReq getEntityInfoForAppReq) {
		GetEntityInfoForAppRes response = null;
        try {
            response = contentComponent.getEntityInfoForApp(getEntityInfoForAppReq);
        } catch (VedantuException e){
            throw e;
        }
        return new VedantuResponse(response);
	}

	@Override
	public VedantuResponse validateResource(GetEntityReq getEntityReq) {
		boolean getEntityRes = false;
        getEntityRes = contentComponent.getEntity(getEntityReq);
        return new VedantuResponse(getEntityRes);
	}

	@Override
	public VedantuResponse getFileInfos(GetFileInfoReq getFileInfoReq) {
		GetFileInfosRes getContentFileRes = null;
        try {
            getContentFileRes = contentComponent.getFileInfo(getFileInfoReq);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(getContentFileRes);
	}

    public Set<BoardBasicInfo> getProgramCourses(List<String> orgIds,
                                                 String programId) throws VedantuException {

        OrgProgram orgProgram = orgProgramRepo.findByIdAndOrgIdIn(programId.trim(),orgIds);

        if(orgProgram==null){
            throw new VedantuException(VedantuErrorCode.INVALID_CODE,"orgProgram found");
        }

        Map<String, BoardBasicInfo> courseInfos = getBasicInfosByIds(orgProgram.courseIds);

        Set<BoardBasicInfo> courseBoardInfos = new HashSet<BoardBasicInfo>();
        if (null != courseInfos) {
            courseBoardInfos.addAll(courseInfos.values());
        }

        return courseBoardInfos;
    }
    public Map<String, BoardBasicInfo> getBasicInfosByIds(Set<String> ids) {
        List<Board> results = boardRepo.findAllByIdIn(ids);
        Map<String, BoardBasicInfo> basicInfoMap = toBasicBoardrInfosMap(results);

        return basicInfoMap;
    }
    public final <B extends ModelBasicInfo> Map<String, B> toBasicBoardrInfosMap(List<Board> results) {

        Map<String, B> infosMap = new LinkedHashMap<String, B>();
        if (CollectionUtils.isNotEmpty(results)) {
            for (Board board : results) {
                if (null == board) {
                    continue;
                }
                infosMap.put(board._getStringId(), (B)new BoardBasicInfo(board));

            }
        }
        return infosMap;
    }
	@Override
	public VedantuResponse getEntityReviews(GetEntityReviewsReq getEntityReviewsReq) {
		GetEntityReviewsRes response = null;
        try {
            response = contentComponent.getEntityReviews(getEntityReviewsReq);
        } catch (VedantuException e){
            throw e;
        }
        return new VedantuResponse(response);

	}

	@Override
	public VedantuResponse addRatingAndFeedback(AddEntityInfoReq addEntityInfoReq) {
		GetEntityInfoForAppRes getEntityRes = null;
        try{
            getEntityRes = contentComponent.addRatingAndFeedback(addEntityInfoReq);
        }catch (VedantuException e){
            throw e;
        }
        return new VedantuResponse(getEntityRes);
	}

    @Override
    public VedantuResponse getCMDSEntityInfo(GetEntityInfoForAppReq getEntityInfoForAppReq) {
        GetCMDSEntityInfoRes response = null;
        try {
            response = contentComponent.getCMDSEntityInfo(getEntityInfoForAppReq);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(response);
    }

    private void addParagraphInfo(List<String> ids, ListResponse<ContentSearchDetails> searchList) {
        Map<String, String> qidToParaMap = new HashMap<String, String>();
        final String htmlBreaks = "<br/><br/>";
        for (String qid : ids) {
            String paraContent = getParaContent(qid);
            if (!StringUtils.isEmpty(paraContent)) {
                qidToParaMap.put(qid, paraContent);
            }
        }
        for (ContentSearchDetails details : searchList.list) {
            logger.debug("Bosa Question subtype: " + details.subType);
            if (QuestionType.PARA.name().equals(details.subType)) {
                String paraContent = qidToParaMap.get(details.id);
                if (StringUtils.isEmpty(paraContent)) {
                    continue;
                }
                logger.debug(":::::   para content " + paraContent);
                logger.debug(":::::   para question content " + details.desc);
                details.name = ImageHTMLUtils.addImageSrcUrl(EntityType.CMDSQUESTION, paraContent)
                        + htmlBreaks + details.name;
                details.desc = ImageHTMLUtils.addImageSrcUrl(EntityType.CMDSQUESTION, paraContent)
                        + htmlBreaks + details.desc;
            }
        }
    }


}
