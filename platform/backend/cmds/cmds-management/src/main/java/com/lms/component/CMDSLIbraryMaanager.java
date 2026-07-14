package com.lms.component;

import com.amazonaws.services.ecs.model.SortOrder;
import com.lms.board.model.GranteeOrgProgram;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.pojos.ScheduleInfo;
import com.lms.common.utils.ObjectIdUtils;
import com.lms.common.vedantu.commons.pojos.requests.OrganizationEntity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.common.vedantu.enums.*;
import com.lms.common.vedantu.mongo.SortOrderInfo;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.components.AnalyticsComponent;
import com.lms.components.CMDSContentManager;
import com.lms.components.CMDSResourcesManager;
import com.lms.components.QuestionComponent;
import com.lms.enums.CmdsContentLinkType;
import com.lms.enums.QuestionType;
import com.lms.enums.SearchResultType;
import com.lms.interfaces.ICMDSModel;
import com.lms.models.*;
import com.lms.models.events.searchdetails.CMDSContentLinkDetails;
import com.lms.models.events.searchdetails.CMDSResourceDetails;
import com.lms.pojo.OrgMemberMappingInfo;
import com.lms.pojo.OrgProgramSectionBasicInfo;
import com.lms.pojo.OrgStructureBasicInfo;
import com.lms.pojo.responce.ActionTakenRes;
import com.lms.pojos.*;
import com.lms.pojos.requests.*;
import com.lms.pojos.responce.*;
import com.lms.repo.CMDSContentLinkRepo;
import com.lms.repo.CMDSModuleRepo;
import com.lms.repo.CMDSTestRepo;
import com.lms.repo.ContentGroupRepo;
import com.lms.repository.*;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class CMDSLIbraryMaanager {
    @Autowired
    private OrgProgramRepo orgProgramRepo;
    @Autowired
    private OrgSectionRepo orgSectionRepo;
    private static final Logger logger = LoggerFactory.getLogger(CMDSLIbraryMaanager.class);
    @Autowired
    private AnalyticsComponent analyticsComponent;
    @Autowired
    private CMDSModuleManager cmdsModuleManager;
    @Autowired
    private CMDSResourcesManager cmdsResourcesManager;
    @Autowired
    private CMDSModuleRepo cmdsModuleRepo;
    @Autowired
    private CMDSTestRepo cmdsTestRepo;
    @Autowired
    private ModuleSchedulesRepo moduleSchedulesRepo;
    @Autowired
    private CMDSContentLinkRepo cmdsContentLinkRepo;
    @Autowired
    private QuestionComponent questionComponent;
    @Autowired
    private AnswerRepo answerRepo;
    @Autowired
    private QuestionRepo questionRepo;
    @Autowired
    private CMDSQuestionRepo cmdsQuestionRepo;
    @Autowired
    private TestRepo testRepo;
    @Autowired
    private ContentGroupRepo contentGroupRepo;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private CMDSContentManager cmdsContentManager;
    @Autowired
    private OrgMemberRepo orgMemberRepo;
    @Autowired
    private OrgCenterRepo orgCenterRepo;
    @Autowired
    private LibraryContentLinksRepo libraryContentLinksRepo;

    public AddToLibraryRes addToLibrary(String userId, String orgId,
                                        List<OrganizationEntity> orgEntities, List<SrcEntity> contents) throws VedantuException {

        for (OrganizationEntity srcEntity : orgEntities) {
            if (!ObjectIdUtils.hasInvalidId(srcEntity.id)) {
                // Check if this Program is Organisations or Not
                if (srcEntity.type == EntityType.PROGRAM) {
                    Optional<OrgProgram> program1 = orgProgramRepo.findById(srcEntity.id);
                    OrgProgram program = program1.get();
                    // Check whether the program orgId and request orgId are equal or not
                    if (!program.orgId.equals(orgId)) {
                        // As program orgId and request orgId are not equal, check if it has sharedProgramAccess
                        if (!hasSharedProgramAccess(srcEntity.id)) {
                            throw new VedantuException(VedantuErrorCode.PRIVATE_PROGRAM, "You can't add content to a shared program");
                        }
                    }
                } else if (srcEntity.type == EntityType.SECTION) {
                    Optional<OrgSection> section1 = orgSectionRepo.findById(srcEntity.id);
                    OrgSection section = section1.get();
                    Optional<OrgProgram> program1 = orgProgramRepo.findById(section.programId);
                    OrgProgram program = program1.get();
                    // Check whether the program orgId and request orgId are equal or not
                    if (!program.orgId.equals(orgId)) {
                        // As program orgId and request orgId are not equal, check if it has sharedProgramAccess
                        if (!hasSharedProgramAccess(srcEntity.id)) {
                            throw new VedantuException(VedantuErrorCode.PRIVATE_PROGRAM, "You can't add content to a shared program");
                        }
                    }
                }
            } else {
                throw new VedantuException(VedantuErrorCode.INCORRECT_ORGANIZATION_ENTITY);
            }
            //
        }

        Set<SrcEntity> collectedSections = getSections(orgId, orgEntities);
        AddToLibraryRes response = new AddToLibraryRes(userId);

        for (SrcEntity orgEntity : collectedSections) {
            //  @SuppressWarnings("unchecked")
            //   if(orgEntity.type== ORGANIZATION)

            // VedantuBasicDAO<? extends VedantuBaseMongoModel, ObjectId> basicDAO = EntityTypeDAOFactory.INSTANCE.get(orgEntity.type);
            // ModelBasicInfo orgEntityInfo = cmdsModuleManager.getBasicInfo(orgEntity.id);

            for (SrcEntity content : contents) {

                try {
                    // checking for valid entitiy types
                    if (!EntityType.isValidOrgEntity(orgEntity.type)) {
                        throw new VedantuException(VedantuErrorCode.UNSUPPORTED_ORG_TYPE);
                    }

                    if (!EntityType.isSupportedCMDSLibraryEntityType(content.type)) {
                        throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
                    }

                    CMDSContentLink createdLink = cmdsModuleManager.addLink(content,
                            orgEntity, CmdsContentLinkType.ADDED, userId, false);

                    CMDSContentLinkDetails libraryContentLinkDetails = new CMDSContentLinkDetails(
                            createdLink._getStringId(), userId, content, orgEntity,
                            createdLink.getScope(), createdLink.timeCreated, createdLink.position);
                    SrcEntity resource = new SrcEntity(EntityType.CMDSRESOURCE,
                            getResourceId(content));
                    //TODO: need to implement
                    //updateUserActionMappintToEs(libraryContentLinkDetails, resource, UserActionType.ADDED, UserActionType.EventActionType.ADD);

                } catch (VedantuException exception) {

                    EntityResponse entityResponse = new EntityResponse(content, exception.errorCode);
                    // entityResponse.orgEntity = orgEntityInfo;

                    logger.debug("Error code: " + exception.errorCode);

                    response.list.add(entityResponse);
                }
            }
            response.totalHits = response.list.size();
        }

        VedantuErrorCode cumulativeErrorCode = EntityResponse.getCumulativeErrorCode(response.list);
        if (cumulativeErrorCode != null) {
            response.cumulativeErrorCode = cumulativeErrorCode;
        }

        return response;
    }

    private boolean hasSharedProgramAccess(String programId) throws VedantuException {
        // TODO Auto-generated method stub
        Optional<OrgProgram> prog = orgProgramRepo.findById(programId);
        return prog.get().sharedProgramAccess;
    }

    private Set<SrcEntity> getSections(String orgId, List<OrganizationEntity> orgEntities)
            throws VedantuException {

        logger.debug("getting section for : " + orgId + ", orgEntities" + orgEntities);
        Set<SrcEntity> collectedSections = new HashSet<SrcEntity>();
        for (OrganizationEntity srcEntity : orgEntities) {
            if (!ObjectIdUtils.hasInvalidId(srcEntity.id)) {
                if (srcEntity.type == EntityType.SECTION) {
                    // TODO check if its valid section
                    collectedSections.add(new SrcEntity(EntityType.SECTION, srcEntity.id));
                } else if (srcEntity.type == EntityType.PROGRAM) {

                    collectedSections = getProgramSections(orgId, srcEntity, collectedSections);

                } else if (srcEntity.type == EntityType.ORGANIZATION) {
                    // TODO to do later
                    // TODO check if its valid organization
                    throw new VedantuException(VedantuErrorCode.INCORRECT_ORGANIZATION_ENTITY);
                }
            } else {
                throw new VedantuException(VedantuErrorCode.INCORRECT_ORGANIZATION_ENTITY);
            }
            //
        }
        return collectedSections;
    }

    private Set<SrcEntity> getProgramSections(String orgId, OrganizationEntity srcEntity,
                                              Set<SrcEntity> entities) throws VedantuException {

        Set<SrcEntity> newCollectedEntities = new HashSet<SrcEntity>();
        List<String> centerIds = new ArrayList<String>();
        if (CollectionUtils.isNotEmpty(srcEntity.centers)) {
            for (SrcEntity center : srcEntity.centers) {
                centerIds.add(center.id);
            }
        }
        Set<String> sectionIds = analyticsComponent.getProgramSections(orgId, srcEntity.id,
                centerIds);
        for (String sectionId : sectionIds) {
            newCollectedEntities.add(new SrcEntity(EntityType.SECTION, sectionId));
        }
        entities.addAll(newCollectedEntities);
        return entities;
    }

    protected String getResourceId(SrcEntity content) {

        return (content.type + "_" + content.id).toLowerCase();
    }

    public AddToLibraryRes removeFromLibrary(String userId, String orgId,
                                             List<OrganizationEntity> orgEntities, List<SrcEntity> contents) throws VedantuException {

        Set<SrcEntity> collectedSections = getSections(orgId, orgEntities);
        AddToLibraryRes response = new AddToLibraryRes(userId);
        ModelBasicInfo orgEntityInfo = null;
        for (SrcEntity orgEntity : collectedSections) {
            if (orgEntity.type == EntityType.SECTION) {
                orgEntityInfo = getBasicSectionInfo(orgEntity.id);
            } else if (orgEntity.type == EntityType.PROGRAM) {
                orgEntityInfo = getBasicProgramInfo(orgEntity.getId());
            }
            //ModelBasicInfo orgEntityInfo = basicDAO.getBasicInfo(orgEntity.id);
            OrgContentVisibleOption option = new OrgContentVisibleOption();
            option.downloadble = false;
            option.visible = false;
            option.encLevel = null;
            option.schedule = null;
            option.orgEntity = orgEntity;

            for (SrcEntity content : contents) {

                try {
                    // checking for valid entity types
                    if (!EntityType.isValidOrgEntity(orgEntity.type)) {
                        throw new VedantuException(VedantuErrorCode.UNSUPPORTED_ORG_TYPE);
                    }

                    if (!EntityType.isSupportedCMDSLibraryEntityType(content.type)) {
                        throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
                    }

//                    if(isPrivateContent(content, orgId)){
//                        throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_REMOVED);
//                    }

                    // make invisible from current orgEntity
                    MakeVisibleReq request = new MakeVisibleReq();
                    request.options.add(option);
                    request.contents.add(content);

                    makeVisible(request, null);

                    // drop link

                    CMDSContentLink removedLink = remove(content,
                            orgEntity, CmdsContentLinkType.ADDED);

                    if (removedLink != null) {
                        // reindexing link
                        reIndex(removedLink);
                    }

                } catch (VedantuException exception) {

                    EntityResponse entityResponse = new EntityResponse(content, exception.errorCode);
                    entityResponse.orgEntity = orgEntityInfo;

                    logger.debug("Error code: " + exception.errorCode);

                    response.list.add(entityResponse);
                }
            }
            response.totalHits = response.list.size();
        }
        VedantuErrorCode cumulativeErrorCode = EntityResponse.getCumulativeErrorCode(response.list);
        if (cumulativeErrorCode != null) {
            response.cumulativeErrorCode = cumulativeErrorCode;
        }
        return response;

    }

    public ModelBasicInfo getBasicProgramInfo(String id) {
        Optional<OrgProgram> pgm = orgProgramRepo.findById(id);
        return pgm.get().toBasicInfo();
    }

    public ModelBasicInfo getBasicSectionInfo(String id) {

        // Map<String, B> infosMap = new LinkedHashMap<String, B>();
        Optional<OrgSection> section = orgSectionRepo.findById(id);
        return section.get().toBasicInfo();


    }

    public MakeVisibleRes makeVisible(MakeVisibleReq request, Map<String, Object> sessionParams) throws VedantuException {

        // check if target Entity is section
        logger.debug("............." + request.options.get(0).downloadableEntities + ".......");
        String orgId = "";
        if (sessionParams != null) {
            orgId = sessionParams.get("orgId").toString();
            String sectionId = request.options.get(0).orgEntity.id;
            Optional<OrgSection> programId1 = orgSectionRepo.findById(sectionId);
            if (!programId1.isPresent()) {
                throw new VedantuException(VedantuErrorCode.INVALID_CODE, "section not found");
            }
            OrgSection orgSection = programId1.get();
            String programId = orgSection.programId;
            Optional<OrgProgram> program1 = orgProgramRepo.findById(programId);
            if (!program1.isPresent()) {
                throw new VedantuException(VedantuErrorCode.INVALID_CODE, "program not found");
            }
            OrgProgram program = program1.get();
            if (!orgId.equals(program.orgId)) {
                if (!program.sharedProgramAccess) {
                    throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_PUBLISHED, "You Can't Publish Content In A Shared Program");
                }
            }
            // Letting Momentum to publish/unpublsh the content in the program we shared.
//            if(orgId.equals("5582b31ae4b0f968650c78d1") && programId.equals("5c7f975be4b0746da4df7340")){
//                // Do nothing. Let this organisation publish/unpublish the content
//                LOGGER.debug("Momentum orgId "+orgId+ " and programId "+programId);
//            }else{
//                // Others. Check whether they are publshing/unpublishing there own content.
//                OrgSectionDAO.INSTANCE.getSectionById(orgId, sectionId);
//            }
        }
        MakeVisibleRes response = new MakeVisibleRes();
        VedantuErrorCode errorCode = null;
        for (OrgContentVisibleOption option : request.options) {
            SrcEntity orgEntity = option.orgEntity;
            ContentGroup contentGroup = null;
            long sizeToBeAdded = 0;
            if (CollectionUtils.isNotEmpty(request.contents)) {
                contentGroup = null;
                Optional<OrgSection> orgSection = orgSectionRepo.findById(orgEntity.getId());
                if (!orgSection.isPresent())
                    throw new VedantuException(VedantuErrorCode.INVALID_CODE, "orgSection not found");
                OrgProgramSectionBasicInfo sectionBasicInfo = new OrgProgramSectionBasicInfo(orgSection.get());

                for (SrcEntity src : request.contents) {
                    // get Link from CMDSContentLink

                    // TODO: we can make it more generic instead of module
                    if (src.type != EntityType.CMDSMODULE) {
                        option.downloadableEntities = null;
                    }
                    logger.debug(" Content " + src + " orgOption" + option);
                    Scope scope = Scope.UNKNOWN;
                    if (option.visible != null) {
                        if (option.visible) {
                            scope = Scope.ORG;
                            // only in case of visible we create content group
                            contentGroup = new ContentGroup();
                            contentGroup.target = option.orgEntity;
                        } else {
                            scope = Scope.PRIVATE;
                        }
                    }

                    //Set/Delete schedules inside a module
                    if (src.type == EntityType.CMDSMODULE && option.visible != null && option.visible) {
                        List<ModuleSchedules> schedules = cmdsModuleManager.getSchedule(orgEntity, src);
                        CMDSModule module = cmdsModuleRepo.findById(src.id).get();

                        SrcEntity globalSource = new SrcEntity(EntityType.MODULE, module.globalModuleId);
                        for (ModuleSchedules schedule : schedules) {
                            logger.error("Module Schedule: " + schedule.globalEntity);
                            CMDSTest cmdsTest = cmdsTestRepo.findById(schedule.entity.id).get();
                            SrcEntity globalEntity = new SrcEntity(EntityType.TEST, cmdsTest.globalTestId);
                            schedule.globalEntity = globalEntity;
                            schedule.globalSource = globalSource;
                            moduleSchedulesRepo.save(schedule);
                        }
                    }

                    ContentWiseVisibilityReport row = new ContentWiseVisibilityReport();

                    row.sectionInfo = sectionBasicInfo;
                    row.content = src;

                    try {

                        if (sectionBasicInfo.sdOnly && option.downloadble != null
                                && option.downloadble) {
                            throw new VedantuException(VedantuErrorCode.NOT_ALLOWED,
                                    "Can not make downloadable");
                        }

                        logger.debug("...........Inside try function...............");
                        AtomicLong totalHits = new AtomicLong(0L);
                        List<CMDSContentLink> cmdsContentLinks = cmdsModuleManager.getCmdsContentLinks(src, orgEntity, CmdsContentLinkType.ADDED,
                                null, 0, 1, VedantuRecordState.ACTIVE, totalHits);
                        if (totalHits.longValue() != 1 && cmdsContentLinks.size() != 1) {
                            logger.debug("There is problem while adding to library");
                            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
                        }
                        CMDSContentLink cmdsContentLink = cmdsContentLinks.get(0);

                        if (option.visible != null) {
                            if (option.visible) {
                                if (option.schedule == null) {
                                    logger.debug("No Schedule Input From Client");
                                    option.schedule = null;
                                } else {
                                    logger.debug("Client provided schedule input");
                                    if (option.schedule.startTime == null) {
                                        logger.debug("Client provided schedule input startTime null" + option.schedule.toString());
                                        option.schedule.startTime = new Date();
                                        logger.debug("Client provided schedule input startTime" + option.schedule.toString());
                                    }
                                }
                                cmdsContentLink.setSchedule(option.schedule);
                            } else {
                                if (cmdsContentLink.getSchedule() != null) {
                                    logger.debug("Setting schedule to null");
                                    option.schedule = null;
                                }
                                cmdsContentLink.setSchedule(option.schedule);
                            }
                        }
                        cmdsContentLinkRepo.save(cmdsContentLink);


                        LibraryContentLink contentLink = null;
                       /* @SuppressWarnings("rawtypes")
                        VedantuBasicDAO vedantuBasicDAO = EntityTypeDAOFactory.INSTANCE
                                .get(src.type);*/
                        if (src.type == EntityType.CMDSMODULE) {
                            logger.debug("...........Inside if statement...............");
                            @SuppressWarnings("rawtypes")


                            ICMDSModel cmdsModel = cmdsModuleRepo.findById(src.id).get();

                            // create Link in LibraryContentLink with globalQId
                            if (cmdsModel.getGlobalId() != null) {
                                SrcEntity globalEntity = new SrcEntity(
                                        src.type._getPublishedType(), cmdsModel.getGlobalId());
                                logger.debug(" Adding global entity "
                                        + src.type._getPublishedType() + " to respective library "
                                        + orgEntity);
                                logger.debug("...........Before add to library...............");
                                contentLink = addToLibrary(globalEntity, orgEntity,
                                        UserActionType.ADDED, request.userId, scope,
                                        option.schedule, null, option.downloadble, option.encLevel,
                                        option.downloadableEntities, cmdsContentLink.position);

                                sizeToBeAdded += ((AbstractContentModel) cmdsModel)
                                        .getExportableSize();
                                // TODO: if required send notification all
                                // members belonging to the target
                                if (option.visible != null && option.visible) {
                                    if (src.type._getPublishedType() != EntityType.QUESTION) {
                                        MadeVisibleDetails details = new MadeVisibleDetails();
                                        details.entity = globalEntity;
                                        details.orgEntity = orgEntity;
                                        details.enableNotifcation(true);
                                        details.userAction = UserActionType.MADE_VISIBLE;
                                        details.userId = request.userId;
                                        questionComponent.generateEventAysc(details.userId, details,
                                                EventType.MADE_VISIBLE);
                                    } else {
                                        contentGroup.contents.add(globalEntity);
                                    }
                                }
                            } else {

                                throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED);
                            }
                        } else {
                            throw new VedantuException(VedantuErrorCode.NOT_VALID_CMDS_CONTENT);

                        }

                        cmdsContentLink.globalLinkId = contentLink._getStringId();
                        CmdsContentLinkType linkedType = null;
                        cmdsContentLink.setScope(contentLink.getScope());
                        if (option.visible != null) {
                            linkedType = option.visible ? CmdsContentLinkType.VISIBLED
                                    : CmdsContentLinkType.INVISIBLED;
                            cmdsModuleManager.addLink(src, orgEntity, linkedType,
                                    request.userId, true);
                        }

                        cmdsContentLink.setDownloadable(contentLink.isDownloadable());
                        if (option.downloadble != null) {
                            linkedType = contentLink.isDownloadable() ? CmdsContentLinkType.DOWNLOAD_ENABLED
                                    : CmdsContentLinkType.DOWNLOAD_DISABLED;
                            cmdsModuleManager.addLink(src, orgEntity, linkedType,
                                    request.userId, true);
                        }
                        if (src.type == EntityType.CMDSMODULE) {
                            cmdsContentLink.setDownloadableEntities(contentLink
                                    .getDownloadableEntities());

                        }

                        cmdsContentLinkRepo.save(cmdsContentLink);

                        row.visibility = cmdsContentLink.getScope();
                        row.errorCode = HardCodedConstants.emptyString;
                        row.downloadable = cmdsContentLink.isDownloadable();
                        row.downloadableEntities = cmdsContentLink.getDownloadableEntities();
                        response.list.add(row);

                    } catch (VedantuException exception) {
                        logger.error(" Making visible failed", exception);

                        row.errorCode = exception.errorCode.name();

                        response.list.add(row);
                        errorCode = exception.errorCode;
                    }
                    response.totalHits++;

                    if (contentGroup != null && CollectionUtils.isNotEmpty(contentGroup.contents)) {

                        contentGroupRepo.save(contentGroup);
                        MadeVisibleDetails details = new MadeVisibleDetails();
                        details.entity = new SrcEntity(EntityType.CONTENTGROUP,
                                contentGroup._getStringId());
                        details.orgEntity = orgEntity;
                        details.enableNotifcation(true);
                        details.userAction = UserActionType.MADE_VISIBLE;
                        details.userId = request.userId;
                        questionComponent.generateEventAysc(details.userId, details, EventType.MADE_VISIBLE);
                    }

                    // TODO can not optimize further as option.visible is a Boolean value

                }// iterated over content
                if (option.visible != null) {
                    addSize(Arrays.asList(orgEntity.id), !option.visible,
                            sizeToBeAdded);
                }
            }// iterate over sections
            // TODO update it using event

        }// contents not null


        if (CollectionUtils.isNotEmpty(request.contents)) {
            logger.debug("Contents are not empty");
            for (SrcEntity entity : request.contents) {
                if (entity.type != EntityType.CMDSTEST) {
                    logger.debug("Content Type is NOT CMDSTEST");
                    continue;
                }
                logger.debug("Content Type is CMDSTEST");
                String cmdsTestId = entity.id;
                logger.debug("CMDSTEST Id is " + cmdsTestId);
                Test test = testRepo.findByCmdsTestId(cmdsTestId);
                if (test == null) {
                    logger.debug("No TEST Object found for CMDSTEST Id" + cmdsTestId);
                    continue;
                }
                logger.debug("Retrieved a TEST Object");
//                test = addSimplifiedBoardNames(test,cmdsTestId);
//                test = setPartialMarkingAndSectionLocking(test,cmdsTestId);
                if (CollectionUtils.isEmpty(test.metadata)) {
                    logger.debug("Cannot find TEST metadata");
                    continue;
                }
                for (TestMetadata metaData : test.metadata) {
                    if (CollectionUtils.isEmpty(metaData.details)) {
                        logger.debug("TEST has metadata but it is empty");
                        continue;
                    }
                    logger.debug("TEST has metadata");
                    for (TestDetails details : metaData.details) {
                        if (details.type != QuestionType.PARA) {
                            logger.debug("TEST metadata details is not of type PARA");
                            continue;
                        }
                        logger.debug("TEST metadata details is of type PARA");
                        List<String> qIds = details.qIds;
                        if (CollectionUtils.isEmpty(qIds)) {
                            continue;
                        }
                        logger.debug("Question Ids of type PARA" + qIds.toString());
                        for (String qId : qIds) {
                            logger.debug("Implementing custom logic for adding custom fields of question id " + qId);
                            Question question = questionRepo.findById(qId).get();
                            Answer answer = answerRepo.findByqId(qId);
                            CMDSQuestion cmdsQuestion = cmdsQuestionRepo.findById(question.cmdsQId).get();

                            if (question == null || answer == null || cmdsQuestion == null) {
                                logger.debug("One of the Question || Answer || CMDSQuestion Object is NULL");
                                continue;
                            }
                            // Change hasTrue field to TRUE
                            // Add field ParagraphId by getting it from CMDSQuestion Table with the help of field cmdsQId
                            MCQsolutionInfo solInfo = (MCQsolutionInfo) cmdsQuestion.solutionInfo;
                            if (CollectionUtils.isNotEmpty(solInfo.answer)) {
                                logger.debug("CMDSQuestion Object has Solutions");
                                List<String> answers = solInfo.answer;
                                if (answers != null && !answers.isEmpty()) {
                                    logger.debug("Setting Answer.answer && Question.hasAns && Question.paragraphId");
                                    answer.answer = answers;
                                    question.hasAns = true;
                                    question.paragraphId = cmdsQuestion.paragraphId;
                                }
                            }
                            logger.debug("Saving Answer and Question Objects");
                            answerRepo.save(answer);
                            questionRepo.save(question);
                            logger.debug("Saved Answer and Question Object");
                        }
                    }

                }

            }
        }

        if (errorCode != null) {
            response.cumulativeErrorCode = errorCode;
        }
        return response;

    }

    public LibraryContentLink addToLibrary(SrcEntity content, SrcEntity targetEntity,
                                           UserActionType linkType, String actorId, Scope scope, ScheduleInfo scheduleInfo,
                                           String parentEsId, Boolean downloadble, EncryptionLevel level,
                                           List<SrcEntity> cmdsDownloadableEntities, long position) throws VedantuException {

       /* logger.debug("Adding " + content + " to library at targetEntity" + targetEntity);

        if (scope == Scope.PRIVATE) {
            // overriding in case scope made private
            downloadble = false;
        }

        List<SrcEntity> downloadableEntities = new ArrayList<SrcEntity>();

        if (cmdsDownloadableEntities != null) {
            for (SrcEntity cmdsEntity : cmdsDownloadableEntities) {

                VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE.get(cmdsEntity.type);

                SrcEntity globalEntity = ((IPublishable) dao).getGlobalEntity(cmdsEntity.id);

                downloadableEntities.add(globalEntity);

            }
        }

        LibraryContentLink link = LibraryContentLinksDAO.INSTANCE.addLink(content, targetEntity,
                linkType, actorId, scope, scheduleInfo, downloadble, level, false,
                downloadableEntities, position);

        logger.debug("............function begin" + link.getDownloadableEntities() + "......"
                + link.position);

        ContentLinkRelationshipDetails libraryContentLinkDetails = new ContentLinkRelationshipDetails(
                link.userId, link.source, link.target, link.getScope());
        libraryContentLinkDetails.schedule = link.getSchedule();
        libraryContentLinkDetails.downloadble = link.isDownloadable();
        libraryContentLinkDetails.encLevel = link.getEncLevel();
        libraryContentLinkDetails.position = position != -1 ? position : 0;

        if (link.getScope() == Scope.PRIVATE) {
            LibraryContentLinksDAO.INSTANCE.updateState(link, VedantuRecordState.DELETED);

            updateUserActionMappintToEs(libraryContentLinkDetails, content, UserActionType.ADDED,
                    EventActionType.REMOVE, parentEsId);
        } else {

            updateUserActionMappintToEs(libraryContentLinkDetails, content, UserActionType.ADDED,
                    EventActionType.ADD, parentEsId);
        }

        // now add the same to contents index
        // TODO: remove this code if we want to go with uniform library structure
        if (ILibraryContent.libraryEntityType.contains(content.type)) {
            QueryBuilder esQuery = QueryBuilders
                    .boolQuery()
                    .must(QueryBuilders.termQuery(ConstantsGlobal.ID, content.id))
                    .must(QueryBuilders.termQuery(ConstantsGlobal.TYPE, content.type.name()
                            .toLowerCase()));
            SearchHit hit = ElasticSearchUtils.findOne(ILibraryContent.INDEX_NAME,
                    ILibraryContent.INDEX_TYPE, esQuery);
            if (hit != null) {
                updateUserActionMappintToEs(libraryContentLinkDetails, content,
                        ILibraryContent.INDEX_NAME, ILibraryContent.INDEX_TYPE,
                        UserActionType.ADDED.getSearchIndexType(), EventActionType.UPDATE,
                        hit.getId());
            }
        }
        logger.debug("............function end" + link.getDownloadableEntities() + "......");
        return link;*/
        return null;

    }

    public boolean addSize(List<String> ids, boolean remove, long size) {

        Query query = new Query();
        Criteria criteria = new Criteria();

        //<OrgSection> sectionQuery = getQuery();
        criteria.and("id").in(ids);
        String sign = "+";
        if (remove) {
            size = -size;
            sign = HardCodedConstants.emptyString;
        }

        Criteria.where("this.size" + sign + size + ">=0");
        List<OrgSection> updateOperations = mongoTemplate.find(query.addCriteria(criteria), OrgSection.class);

        OrgSection orgSection = updateOperations.get(0);
        orgSection.setSize(orgSection.getSize() + 1);

        orgSectionRepo.save(orgSection);

        return true;
    }

    public CMDSContentLink remove(SrcEntity content, SrcEntity targetEntity,
                                  CmdsContentLinkType linkType) throws VedantuException {

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("target").is(targetEntity);
        criteria.and("source").is(content);
        criteria.and("linkType").is(linkType);

        List<CMDSContentLink> getContentQuery = mongoTemplate.find(query.addCriteria(criteria), CMDSContentLink.class);

        CMDSContentLink cmdsContentLink = getContentQuery.get(0);
        cmdsContentLink.setRecordState(VedantuRecordState.DELETED);
        cmdsContentLinkRepo.save(cmdsContentLink);


        if (cmdsContentLink != null) {
            cmdsContentLink.recordState = VedantuRecordState.DELETED;
        }
        return cmdsContentLink;
    }

    public void reIndex(CMDSContentLink link) {

        CMDSContentLinkDetails libraryContentLinkDetails = new CMDSContentLinkDetails(
                link._getStringId(), link.userId, link.source, link.target, link.getScope(),
                link.timeCreated, link.position);
        SrcEntity resource = new SrcEntity(EntityType.CMDSRESOURCE, getResourceId(link.source));
        if (link.recordState == VedantuRecordState.ACTIVE) {

            cmdsModuleManager.updateUserActionMappintToEs(libraryContentLinkDetails, resource, link.linkType.name()
                    .toLowerCase(), UserActionType.EventActionType.UPDATE, null);
        } else {
            cmdsModuleManager.updateUserActionMappintToEs(libraryContentLinkDetails, resource, link.linkType.name()
                    .toLowerCase(), UserActionType.EventActionType.REMOVE, null);
        }
    }

    public GetLibraryResourceRes getResources2(GetLibraryResourcesReq request)
            throws VedantuException {

        // check in EC
        GetLibraryResourceRes response = new GetLibraryResourceRes();
        logger.debug("Getting Libary resources in section Id " + request.orgEntity);
        SrcEntity entityAtRequestedLevel = request.orgEntity;
        if (CollectionUtils.isNotEmpty(request.orgEntity.centers)) {
            // getting first center as this CALL meant only for one library;
            entityAtRequestedLevel = request.orgEntity.centers.get(0);
        }
        try {

            // collect all sections

            List<OrganizationEntity> orgEntities = new ArrayList<OrganizationEntity>();
            orgEntities.add(request.orgEntity);
            Set<SrcEntity> sectionEntities = null;

            if (!StringUtils.isEmpty(request.orgEntity.id)) {

                if (ObjectIdUtils.hasInvalidId(request.orgEntity.id)) {
                    throw new VedantuException(VedantuErrorCode.INCORRECT_ORGANIZATION_ENTITY);
                }

                sectionEntities = getSections(request.orgId, orgEntities);

            }

            Set<String> sections = new HashSet<String>();
            for (SrcEntity sectionEntity : sectionEntities) {
                sections.add(sectionEntity.id);
            }

            List<ModelBasicInfo> basicInfos = new ArrayList<ModelBasicInfo>();

            SrcEntity contentSrc = new SrcEntity(EntityType.ORGANIZATION, request.orgId);

            //Get all the orgIds that gave access to the current organization
            AtomicLong totalProgramHits = new AtomicLong(0L);
            List<GranteeOrgProgram> granteeOrgPrograms = cmdsResourcesManager.getGranteeOrgPrograms(request.orgId, null, totalProgramHits);
            List<String> grantedOrgs = new ArrayList<String>();
            for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
                grantedOrgs.add(granteeOrgProgram.providerOrgId);
            }
            Query query = new Query();
            Criteria criteria = new Criteria();


            cmdsContentManager.buildSearchQuery(SearchResultType.ALL, request.userId,
                    contentSrc, null, null, null, request.query, null, grantedOrgs, query, criteria);

            cmdsContentManager.addBoardAndTargetFilter(request.brdIds, false, query, criteria);

            Query targetQuery = new Query();
            Criteria criteria1 = new Criteria();

            Query targetIdTypeQuery = new Query();
            Criteria criteria2 = new Criteria();
            criteria1.and("target.id").is(sections.toArray());

            Query targetTypeQueryBuilder = new Query();
            Criteria criteria3 = new Criteria();
            criteria1.and("target.type").is(EntityType.SECTION.name().toLowerCase());


            Set<String> entityTypeSet = new HashSet<String>();
            if (CollectionUtils.isNotEmpty(request.includes)) {
                for (EntityType include : request.includes) {
                    entityTypeSet.add(include.name().toLowerCase());
                }

                Query inContentTypeQuery = new Query();

                criteria1.and("source.type").is(entityTypeSet.toArray());
                //targetQuery.addCriteria(inContentTypeQuery);

                criteria1.and("content.type").is(entityTypeSet.toArray());

            }

            entityTypeSet.clear();
            if (CollectionUtils.isNotEmpty(request.excludes)) {
                entityTypeSet = new HashSet<String>();
                for (EntityType exclude : request.excludes) {
                    entityTypeSet.add(exclude.name().toLowerCase());
                }
                criteria1.and("source.type").nin(entityTypeSet.toArray());


                criteria1.and("content.type").nin(entityTypeSet.toArray());

            }
            targetQuery.addCriteria(criteria1);


            List<CMDSContentLinkDetails> searchResults = mongoTemplate.find(targetQuery, CMDSContentLinkDetails.class);

            List<String> details = new ArrayList<String>();

            response.totalHits = getLinkIds(searchResults, details);

            List<CMDSContentLink> contentLinks = cmdsContentLinkRepo.findAllByIdAndRecordState(details, VedantuRecordState.ACTIVE);


            basicInfos = cmdsContentManager.getBasicInfoFromLinks(contentLinks, basicInfos);
            response.list.addAll(basicInfos);

        } catch (Exception exception) {
            logger.debug(" Error", exception);
            logger.debug(" Error", exception);
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }
        // check in
        return response;
    }

    public long getLinkIds(List<CMDSContentLinkDetails> response, List<String> links) {

        if (response == null || response.stream().count() == 0) {
            logger.error("empty search response for query : ");
            return 0;
        }
        logger.debug(" Search responses " + response.size());


        long totalHits = response.size();
        logger.debug("totalHits: " + totalHits);
        for (CMDSContentLinkDetails hits : response) {

            links.add(hits.id);
        }

        return totalHits;
    }

    public GetLibraryResourceRes getResources(GetLibraryResourcesReq request)
            throws VedantuException {

        // check in EC
        GetLibraryResourceRes response = new GetLibraryResourceRes();
        logger.debug("Getting Libary resources in folder Id " + request.orgEntity);
        SrcEntity entityAtRequestedLevel = request.orgEntity;
        if (CollectionUtils.isNotEmpty(request.orgEntity.centers)) {
            // getting first center as this CALL meant only for one library;
            entityAtRequestedLevel = request.orgEntity.centers.get(0);
        }
        try {

            // collect all sections

            List<OrganizationEntity> orgEntities = new ArrayList<OrganizationEntity>();
            orgEntities.add(request.orgEntity);
            Set<SrcEntity> sectionEntities = null;

            if (!StringUtils.isEmpty(request.orgEntity.id)) {

                if (ObjectIdUtils.hasInvalidId(request.orgEntity.id) /*
                 * TODO || check for invalid
                 * entity type
                 */) {
                    throw new VedantuException(VedantuErrorCode.INCORRECT_ORGANIZATION_ENTITY);
                }

                sectionEntities = getSections(request.orgId, orgEntities);

            }

            Set<String> sections = new HashSet<String>();
            for (SrcEntity sectionEntity : sectionEntities) {
                sections.add(sectionEntity.id);
            }

            List<ModelBasicInfo> basicInfos = new ArrayList<ModelBasicInfo>();

            SrcEntity contentSrc = new SrcEntity(EntityType.ORGANIZATION, request.orgId);

            //Get all the orgIds that gave access to the current organization
            AtomicLong totalProgramHits = new AtomicLong(0L);
            List<GranteeOrgProgram> granteeOrgPrograms = cmdsResourcesManager.getGranteeOrgPrograms(request.orgId, null, totalProgramHits);
            List<String> grantedOrgs = new ArrayList<String>();
            for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
                grantedOrgs.add(granteeOrgProgram.providerOrgId);
            }
            Query query = new Query();
            Criteria criteria = new Criteria();
            cmdsContentManager.buildSearchQuery(SearchResultType.ALL, request.userId,
                    contentSrc, null, null, null, request.query, null, grantedOrgs, query, criteria);

            cmdsContentManager.addBoardAndTargetFilter(request.brdIds, false, query, criteria);

            Query targetQuery = new Query();
            Criteria criteria1 = new Criteria();

            Query targetIdTypeQuery = new Query();
            Criteria criteria2 = new Criteria();
            criteria1.and("target.id").is(sections.toArray());

            Query targetTypeQueryBuilder = new Query();
            Criteria criteria3 = new Criteria();
            criteria1.and("target.type").is(EntityType.SECTION.name().toLowerCase());


            Set<String> entityTypeSet = new HashSet<String>();
            if (CollectionUtils.isNotEmpty(request.includes)) {
                for (EntityType include : request.includes) {
                    entityTypeSet.add(include.name().toLowerCase());
                }

                Query inContentTypeQuery = new Query();

                criteria1.and("source.type").is(entityTypeSet.toArray());
                //targetQuery.addCriteria(inContentTypeQuery);

                criteria1.and("content.type").is(entityTypeSet.toArray());

            }

            entityTypeSet.clear();
            if (CollectionUtils.isNotEmpty(request.excludes)) {
                entityTypeSet = new HashSet<String>();
                for (EntityType exclude : request.excludes) {
                    entityTypeSet.add(exclude.name().toLowerCase());
                }
                criteria1.and("source.type").nin(entityTypeSet.toArray());


                criteria1.and("content.type").nin(entityTypeSet.toArray());

            }
            targetQuery.addCriteria(criteria1);


            List<CMDSContentLinkDetails> searchResults = mongoTemplate.find(targetQuery, CMDSContentLinkDetails.class);

            List<CMDSResourceDetails> details = new ArrayList<CMDSResourceDetails>();

            response.totalHits = getBasicInfoFromESSearch(searchResults, details, null);

            List<CMDSContentLink> links = new ArrayList<CMDSContentLink>();

            AtomicLong totalHits = new AtomicLong(0L);
            CMDSContentLink link = null;

            for (CMDSResourceDetails detail : details) {

                if (entityAtRequestedLevel.type == EntityType.SECTION) {
                    SrcEntity sectionEntity = new SrcEntity(EntityType.SECTION,
                            entityAtRequestedLevel.id);
                    List<CMDSContentLink> testLinks = cmdsModuleManager.getCmdsContentLinks(detail.content, sectionEntity,
                            CmdsContentLinkType.ADDED, null, 0, 1,
                            VedantuRecordState.ACTIVE, totalHits);
                    if (CollectionUtils.isNotEmpty(testLinks)) {
                        link = testLinks.get(0);
                        logger.debug(" Found link for section" + link);

                    } else {
                        logger.error(" Mismatch in ES and MONGODB results ");
                    }
                } else {
                    link = new CMDSContentLink();
                    link.source = detail.content;
                    link.target = entityAtRequestedLevel;
                    link.linkType = CmdsContentLinkType.ADDED;

                }

                links.add(link);
            }

            basicInfos = cmdsContentManager.getBasicInfoFromLinks(links, basicInfos);
            response.list.addAll(basicInfos);

        } catch (Exception exception) {
            logger.debug(" Error", exception);
            logger.debug(" Error", exception);
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
        }
        // check in
        return response;
    }

    private long getBasicInfoFromESSearch(List<CMDSContentLinkDetails> response, List<CMDSResourceDetails> resources, String type) {

        if (response == null || response.stream().count() == 0) {
            logger.debug("empty search response for query : ");
            return 0;
        }
        logger.debug(" Search responses " + response.stream().count());


        long totalHits = response.stream().count();
        logger.debug("totalHits: " + totalHits);
        for (CMDSContentLinkDetails hits : response) {
            //CMDSResourcesManager.LOGGER.debug("hits : " + hits.sourceAsString());
            CMDSResourceDetails model = new CMDSResourceDetails();

            if (type != null && type.equals("CMDSQUESTION")) {
                SrcEntity content = new SrcEntity();
                content.id = model.id;
                content.type = EntityType.CMDSQUESTION;

                model.content = content;
                model.id = "cmdsquestion_" + model.id;
            }
            resources.add(model);
        }

        return totalHits;
    }

    public GetVisibilityChartRes getContentVisibilityReport(GetVisibilityChartReq request)
            throws VedantuException {

        AtomicLong totalHits = new AtomicLong(0L);

        GetVisibilityChartRes response = new GetVisibilityChartRes();
        // TODO restrict types
      /*  ModelBasicInfo info = EntityTypeDAOFactory.INSTANCE.get(request.content.type).getBasicInfo(
                request.content.id);
        if (info == null) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }*/

        OrgMember orgMember = getMemberByUserId(request.orgId, request.userId);

        if (orgMember == null) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        List<String> sectionIds = new ArrayList<String>();
        if (CollectionUtils.isNotEmpty(orgMember.mappings)) {
            for (OrgMemberMappingInfo mapping : orgMember.mappings) {
                sectionIds.add(mapping.sectionId);
            }
            logger.debug("Collected section ids" + sectionIds);
        }
        List<VisibilityReport> report = getContentVisibilityReport(sectionIds, new SrcEntity(
                        request.content.type, request.content.id), CmdsContentLinkType.ADDED,
                request.start, request.size, totalHits);

        //Collections.sort(report, VisibilityReportComparator.INSTANCE);
        response.list.addAll(report);
        response.totalHits = totalHits.longValue();

        return response;

    }

    public OrgMember getMemberByUserId(String orgId, String userId) {
        logger.debug("getMemberByUserId orgId: " + orgId + ", userId: " + userId);

        OrgMember orgMember = orgMemberRepo.findByOrgIdAndUserId(orgId, userId);


        if (orgMember == null) {
            logger.error("cannot find orgMember for orgId: " + orgId + ", userId: " + userId);
        }
        return orgMember;
    }

    public List<VisibilityReport> getContentVisibilityReport(List<String> sectionIds,
                                                             SrcEntity content, CmdsContentLinkType linkType, int start, int size,
                                                             AtomicLong totalHits) {

        // TODO update it use only sectionIds;

        List<VisibilityReport> visibilityReports = new ArrayList<VisibilityReport>();
        List<SrcEntity> targets = new ArrayList<SrcEntity>();
        if (CollectionUtils.isEmpty(sectionIds)) {
            return visibilityReports;
        }

        for (String sectionId : sectionIds) {
            targets.add(new SrcEntity(EntityType.SECTION, sectionId));
        }
        List<CMDSContentLink> links = getCmdsContentLinksForTargets(content, targets, linkType, null, start, size,
                VedantuRecordState.ACTIVE, totalHits);

        if (CollectionUtils.isNotEmpty(links)) {
            for (CMDSContentLink link : links) {
                logger.debug(link.target.id + " type " + link.target.type);

                VisibilityReport row = null;
                row = createVisibilityReport(link.target.id);
                row.visibility = link.getScope();
                row.downloadable = link.isDownloadable();
                row.downloadableEntities = link.getDownloadableEntities();
                visibilityReports.add(row);
            }
        }

        return visibilityReports;
    }

    public List<CMDSContentLink> getCmdsContentLinksForTargets(SrcEntity content,
                                                               List<SrcEntity> targetEntities, CmdsContentLinkType linkType, String actorId,
                                                               int start, int size, VedantuRecordState recordState, AtomicLong totalHits) {

        logger.debug("Geting links by targets");
        Query query = new Query();
        Criteria criteria = new Criteria();
        // Query<CMDSContentLink> findQuery = getQuery();

        Set<String> targetTypes = new HashSet<String>();
        Set<String> targetIds = new HashSet<String>();
        for (SrcEntity target : targetEntities) {
            targetTypes.add(target.type.name());
            targetIds.add(target.id);

        }
        criteria.and("content").is(content);
        logger.debug("target ids" + targetIds);
        criteria.and("target.type").in(targetTypes);
        criteria.and("target.id").in(targetIds);
        criteria.and("linkType").equals(linkType);
        if (!StringUtils.isEmpty(actorId)) {
            criteria.and("actorId").equals(actorId);
        }
        if (recordState != null) {
            criteria.and("recordState").equals(recordState);
        }
        query.addCriteria(criteria);
        List<CMDSContentLink> cmdsContentLinks = mongoTemplate.find(query, CMDSContentLink.class);
        totalHits.set(cmdsContentLinks.size());

        return cmdsContentLinks;
    }

    private VisibilityReport createVisibilityReport(String sectionId) {

        OrgSection section = orgSectionRepo.findById(sectionId).get();
        VisibilityReport row = new VisibilityReport();

        logger.debug("id " + section._getStringId() + " centerId " + section.centerId);

        row.programInfo = new OrgStructureBasicInfo(orgProgramRepo.findById(section.programId).get());
        row.centerInfo = new OrgStructureBasicInfo(orgCenterRepo.findById(section.centerId).get());

        row.sectionInfo = new OrgStructureBasicInfo(section._getStringId(), section.recordState,
                section.getName(), section.code, EntityType.SECTION);

        return row;
    }

    public ActionTakenRes updateLocation(UpdateRankReq request) throws VedantuException {

        // TODO start with zookeeper lock or simple mongo local to handle or check on section
        // library version updates

        ActionTakenRes response = new ActionTakenRes();
        response.done = false;

        AtomicLong totalHits = new AtomicLong();
        List<CMDSContentLink> links = cmdsModuleManager.getCmdsContentLinks(
                request.entity, request.target, CmdsContentLinkType.ADDED, null, Scope.UNKNOWN, 0,
                1, VedantuRecordState.ACTIVE, totalHits, null);

        if (CollectionUtils.isEmpty(links)) {
            throw new VedantuException(VedantuErrorCode.NOT_ALLOWED);

        }

        CMDSContentLink link = links.get(0);
        if (link.position != request.moveFrom) {
            throw new VedantuException(VedantuErrorCode.NOT_ALLOWED);
        }
        Query query = new Query();
        Criteria criteria = new Criteria();
        // Query<CMDSContentLink> findQuery = CmdsContentLinkDAO.INSTANCE.createQuery();
        if (request.moveFrom == request.moveTo) {
            return response;
        }

        // move ahead moveFrom <= moveTo
        // decrement all between greater than moveFrom and equal to moveTo
        // update current link to moveTo

        boolean moveDown = false;
        if (request.moveFrom < request.moveTo) {

            criteria.and(CMDSContentLink.POSITION).gt(request.moveFrom);
            criteria.and(CMDSContentLink.POSITION).lte(request.moveTo);
        } else {
            moveDown = true;

            criteria.and(CMDSContentLink.POSITION).gt(request.moveTo);
            criteria.and(CMDSContentLink.POSITION).lte(request.moveFrom);

        }
        // move back moveFrom >= moveTo
        // increment all between greater than equal to moveTo equal To moveFrom

        String order = new SortOrderInfo(SortOrder.DESC, CMDSContentLink.POSITION).toString();
        criteria.and(order);

        List<CMDSContentLink> updatableLinks = mongoTemplate.find(query.addCriteria(criteria), CMDSContentLink.class);
        List<String> positionFieldUpdateArray = Arrays.asList(CMDSContentLink.POSITION);
        LibraryContentLink dummyLink = new LibraryContentLink();
        List<String> libraryListCollectors = new ArrayList<String>();
        for (CMDSContentLink updatableLink : updatableLinks) {

            if (moveDown) {
                updatableLink.position += updatableLink.position > 0 ? 1 : 0;
            } else {
                updatableLink.position -= 1;
            }

            cmdsContentLinkRepo.save(updatableLink);
            reIndex(updatableLink);
            logger.debug("Check globalLink " + updatableLink.globalLinkId);
            if (!StringUtils.isEmpty(updatableLink.globalLinkId)) {
                dummyLink.position = updatableLink.position;
                dummyLink.id = new ObjectId(updatableLink.globalLinkId);
                libraryContentLinksRepo.save(dummyLink);
                libraryListCollectors.add(updatableLink.globalLinkId);
            }
        }

        // UpdateOperations<CMDSContentLink> rankUpdater = CmdsContentLinkDAO.INSTANCE
        // .createUpdateOperations();
        //
        // rankUpdater.inc(CMDSContentLink.POSITION);
        // UpdateResults<CMDSContentLink> updateResults = CmdsContentLinkDAO.INSTANCE.update(
        // findQuery, rankUpdater);
        link.position = request.moveTo;
        cmdsContentLinkRepo.save(link);
        reIndex(link);
        if (!StringUtils.isEmpty(link.globalLinkId)) {
            dummyLink.position = link.position;
            dummyLink.id = new ObjectId(link.globalLinkId);
            logger.debug("Check globalLink " + link.globalLinkId);
            libraryContentLinksRepo.save(dummyLink);
            libraryListCollectors.add(link.globalLinkId);
        }
        // TODO As of now this index updating is not being used any where
        // ReIndexLibraryContentReq libraryRequestUpdateReq = new ReIndexLibraryContentReq();
        // libraryRequestUpdateReq.linkIds= libraryListCollectors;
        // IndexingManager.INSTANCE.reIndexLibraryContentLinks(libraryRequestUpdateReq);
        response.done = true;
        return response;
    }
}
