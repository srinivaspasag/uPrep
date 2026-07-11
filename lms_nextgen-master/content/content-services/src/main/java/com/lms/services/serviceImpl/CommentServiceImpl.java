package com.lms.services.serviceImpl;

import com.lms.billing.model.TeacherAnalytics;
import com.lms.billing.repository.TeacherAnalyticsRepo;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.relationships.EntityUserActionRelationshipSearchDetails;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.enums.*;
import com.lms.enums.CommentType;
import com.lms.enums.DoubtState;
import com.lms.enums.OrgMemberProfile;
import com.lms.managers.AbstractContentManager;
import com.lms.models.*;
import com.lms.pojo.*;
import com.lms.pojos.CommentDetails;
import com.lms.pojos.requests.AddCommentReq;
import com.lms.pojos.requests.GetCommentReq;
import com.lms.pojos.requests.GetCommentsReq;
import com.lms.pojos.responce.AbstractContentUserActionRes;
import com.lms.pojos.responce.AddCommentRes;
import com.lms.pojos.responce.GetCommentRes;
import com.lms.pojos.responce.GetCommentsRes;
import com.lms.repository.*;
import com.lms.services.CommentService;
import com.lms.user.vedantu.user.model.User;
import com.lms.user.vedantu.user.pojo.UserInfo;
import com.lms.utils.EntityUserActionUtils;
import com.lms.utils.ISocialEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class CommentServiceImpl extends AbstractContentManager implements CommentService {
    private final static Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Autowired
    private CommentRepo commentRepo;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private OrgMemberRepo orgMemberRepo;
    @Autowired
    private OrgProgramRepo orgProgramRepo;
    @Autowired
    private OrgCenterRepo orgCenterRepo;
    @Autowired
    private OrgSectionRepo orgSectionRepo;
    @Autowired
    private OrgDepartmentRepo orgDepartmentRepo;
    @Autowired
    private EntityUserActionMappingRepo entityUserActionMappingRepo;
    @Autowired
    private DoubtTransactionRepo doubtTransactionRepo;
    @Autowired
    private TeacherAnalyticsRepo teacherAnalyticsRepo;
    @Autowired
    private EntityUserActionUtils entityUserActionUtils;

    @Override
    public VedantuResponse addcomment(AddCommentReq addCommentReq) {
        if (addCommentReq.getParent() == null || addCommentReq.getParent().getType() == null || addCommentReq.getParent().getId() == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "please provide parent.type or parent.id");
        }

        AddCommentRes addCommentRes = addComment(addCommentReq);


        return new VedantuResponse(addCommentRes);
    }

    @Override
    public VedantuResponse getcomment(GetCommentReq getCommentReq) {
        if (getCommentReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetCommentRes getCommentRes = getComment(getCommentReq);

        return new VedantuResponse(getCommentRes);
    }

    @Override
    public VedantuResponse getcomments(GetCommentsReq getCommentsReq) {
        if (getCommentsReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetCommentsRes getCommentsRes = getComments(getCommentsReq);

        return new VedantuResponse(getCommentsRes);
    }

    private GetCommentsRes getComments(GetCommentsReq getCommentsReq) {
        if (!getCommentsReq.validateRequestParams()) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        List<Comment> comments = getComments(getCommentsReq.parent,
                getCommentsReq.start, getCommentsReq.size, getCommentsReq.orderBy,
                getCommentsReq.sortOrder);
        logger.debug("getComments response: " + comments);
        return annotateUserActionData(null, getCommentsReq.userId, comments);
    }

    private GetCommentRes getComment(GetCommentReq getCommentReq) {
        Optional<Comment> comment = commentRepo.findById(getCommentReq.getId());
        //CommentDAO.INSTANCE.getComment(getCommentReq.id);
        if (!comment.isPresent()) {
            throw new VedantuException(VedantuErrorCode.COMMENT_NOT_FOUND);
        }
        return annotateUserActionData(comment.get().contentSrc != null && comment.get().contentSrc.type == EntityType.ORGANIZATION ? comment.get().getContentSrc().getId() : null, comment.get());
    }

    private AddCommentRes addComment(AddCommentReq addCommentReq) {
        isSocialActionAllowed(addCommentReq.parent.type, addCommentReq.parent.id);
        try {
            addCommentReq.removeImageSrc(true);
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
        }
        Comment comment = addComment(addCommentReq.base, addCommentReq.root,
                addCommentReq.parent, addCommentReq.scope, addCommentReq.userId,
                addCommentReq.content, addCommentReq.tags != null ? new HashSet<String>(
                        addCommentReq.tags) : new HashSet<String>(), addCommentReq.type);

        updateParentCommentsCount(comment.userId, comment.parent);

        EntityUserActionMapping actionMapping = entityUserActionUtils.addEntityUserActionMapping(comment.getUserId(), UserActionType.COMMENTED,
                addCommentReq.parent, true);

        if (addCommentReq.parent.type != EntityType.COMMENT
                && addCommentReq.parent.type != EntityType.SOLUTION) {
            EntityUserActionRelationshipSearchDetails userActionDetails = new EntityUserActionRelationshipSearchDetails(
                    addCommentReq.userId, addCommentReq.parent.id);
            updateUserActionMappintToEs(userActionDetails, addCommentReq.parent,
                    UserActionType.COMMENTED, UserActionType.EventActionType.ADD, null);
        }
        //
        // CommentDetails details = new CommentDetails(addCommentReq.userId, EventType.ADD_COMMENT,
        // new SrcEntity(EntityType.COMMENT, comment._getStringId()),
        // actionMapping._getStringId(), comment._getStringId());

        CommentDetails details = new CommentDetails(addCommentReq.userId, EventType.ADD_COMMENT,
                addCommentReq.parent, actionMapping._getStringId(), comment._getStringId());

        details.commentText = comment.content;
        generateEventAysc(addCommentReq.userId, details, EventType.ADD_COMMENT);

        OrgMember member = orgMemberRepo.findByUserId(addCommentReq.getUserId());

        if (member.profile == OrgMemberProfile.TEACHER) {
            markDoubtAsSolved(addCommentReq.parent.id, member._getStringId());
        }

        return annotateUserActionData(comment.contentSrc != null
                && comment.contentSrc.type == EntityType.ORGANIZATION ? comment.contentSrc.id
                : null, comment);
    }

    protected static void addSocialActionInfo(String userId, String entityOwnerId,
                                              Map<String, Boolean> entityVoteMap, Map<String, FollowType> followTypeMap,
                                              ISocialEntity entity) {

        entity._setVoted(entityVoteMap != null && entityVoteMap.get(entity._getEntityId()) != null && entityVoteMap
                .get(entity._getEntityId()).booleanValue());
        FollowType followType = userId.equals(entityOwnerId) ? FollowType.OWNER
                : (followTypeMap != null && followTypeMap.get(entity._getEntityId()) != null ? followTypeMap
                .get(entity._getEntityId()) : FollowType.NONE);
        entity._setFollowType(followType);
    }

    public Map<String, ModelBasicInfo> getUserInfoMap(String orgId,
                                                      Collection<String> userIds) {

        return getUserInfoMap(orgId, userIds, false);
    }

    private AddCommentRes annotateUserActionData(String orgId, Comment comment) {

        AddCommentRes addCommentRes = new AddCommentRes(comment._getStringId(), comment.upVotes,
                comment.views, comment.followers, comment.comments, comment.timeCreated,
                comment.lastUpdated, false, comment.content, comment.parent, comment.type,
                comment.hasMedia, comment.base, comment.root, comment.scope, comment.tags);
        addCommentRes.user = (UserInfo) getUserInfoMap(orgId, Arrays.asList(comment.userId)).get(
                comment.userId);
        //TODO:need to implement
        //addCommentRes.addImageSrcUrl();
        return addCommentRes;
    }
    private  Map<String, ModelBasicInfo> populateOrgMemberInfo(List<OrgMember> orgMembers,
                                                                     boolean excludeOrgMappingInfo) {

        Set<String> centerIds = new HashSet<String>();
        Set<String> sectionIds = new HashSet<String>();
        Set<String> programIds = new HashSet<String>();

        if (!excludeOrgMappingInfo) {
            for (OrgMember orgMember : orgMembers) {
                if (orgMember.mappings == null) {
                    continue;
                }
                for (OrgMemberMappingInfo mapping : orgMember.mappings) {
                    if (null == mapping) {
                        continue;
                    }
                    programIds.add(mapping.programId);
                    centerIds.add(mapping.centerId);
                    sectionIds.add(mapping.sectionId);
                    // if (mapping.courseIds != null) {
                    // courseIds.addAll(mapping.courseIds);
                    // }
                }
            }
        }

        logger.debug("programIds : " + programIds + " excludeMappingInfo : "
                + excludeOrgMappingInfo);
        logger.debug("centerIds : " + centerIds);
        logger.debug("sectionIds : " + sectionIds);
        Map<String, ModelBasicInfo> orgComponentBasicInfoMap = new HashMap<String, ModelBasicInfo>();
        // collect program info
        if (!excludeOrgMappingInfo) {

            List<OrgProgram> orgPrograms= orgProgramRepo.findAllByIdIn(programIds);
            if(!orgPrograms.isEmpty())
                         orgComponentBasicInfoMap.putAll(toBasicInfosMap(orgPrograms));


            // collect center info
            List<OrgCenter> orgCenters= orgCenterRepo.findAllByIdIn(centerIds);
            if(!orgCenters.isEmpty())
                orgComponentBasicInfoMap.putAll(toBasicCenterInfosMap(orgCenters));

            // collect section info

            List<OrgSection> orgSections = orgSectionRepo.findAllByIdIn(sectionIds);
            if(!orgSections.isEmpty())
                orgComponentBasicInfoMap.putAll(toBasicSectionInfosMap(orgSections));


        }
        Map<String, ModelBasicInfo> userInfoMap = new HashMap<String, ModelBasicInfo>();

        for (OrgMember orgMember : orgMembers) {
            OrgMemberBasicInfo orgMemberBasicInfo = (OrgMemberBasicInfo) orgMember.toBasicInfo();
            if (!excludeOrgMappingInfo && orgMember.mappings != null) {
                for (OrgMemberMappingInfo mapping : orgMember.mappings) {
                    if (null == mapping) {
                        continue;
                    }
                    OrgStructureBasicInfo program = (OrgStructureBasicInfo) orgComponentBasicInfoMap
                            .get(mapping.programId);
                    logger.debug("programId : " + mapping.programId);
                    if (program == null) {
                        continue;
                    }

                    OrgProgramBasicInfo programInfo = orgMemberBasicInfo.mappings
                            ._getOrAddProgram(program);

                    OrgStructureBasicInfo progCenter = (OrgStructureBasicInfo) orgComponentBasicInfoMap
                            .get(mapping.centerId);
                    OrgProgramCenterBasicInfo progCenterInfo = programInfo
                            ._getOrAddProgramCenter(progCenter);

                    OrgStructureBasicInfo progSection = (OrgStructureBasicInfo) orgComponentBasicInfoMap
                            .get(mapping.sectionId);
                    OrgProgramSectionBasicInfo progSectionInfo = progCenterInfo
                            ._getOrAddProgramSection(progSection);
                    logger.debug("OrgProgramSectionBasicInfo :" + progSectionInfo);
                }
            }
            userInfoMap.put(orgMember.userId, orgMemberBasicInfo);
        }

        return userInfoMap;
    }



    public ModelBasicInfo toProgramBasicInfo(OrgProgram orgProgram) {

        Optional<OrgDepartment> department = orgDepartmentRepo.findById(orgProgram.getDepartmentId().trim());
        if(!department.isPresent())
            throw new VedantuException(VedantuErrorCode.INVALID_ACCESS_CODE,"Department is not found");

        return new OrgProgramBasicInfo(orgProgram._getStringId(), orgProgram.getRecordState(), orgProgram.getcName(), orgProgram.getCode(),
                orgProgram._getEntityType(), orgProgram.getDepartmentId(), department.get().getName(), department.get().getCode(), orgProgram.getCourseIds(), orgProgram.isOffline);
    }
    public final <B extends ModelBasicInfo> Map<String, B> toBasicInfosMap(List<OrgProgram> results) {

        Map<String, B> infosMap = new LinkedHashMap<String, B>();
        if (CollectionUtils.isNotEmpty(results)) {
            for (OrgProgram orgProgram : results) {
                if (null == orgProgram) {
                    continue;
                }
                infosMap.put(orgProgram._getStringId(), (B)toProgramBasicInfo(orgProgram));

            }
        }
        return infosMap;
    }


    public List<Comment> getComments(SrcEntity parent, int start,
                                     int size, String orderBy, String sortOrder) {

        List<Comment> comments = commentRepo.findAllByParent(parent);
        return comments;
    }
    private  GetCommentsRes annotateUserActionData(String orgId, String userId,
                                                         List<Comment> comments) {

        GetCommentsRes getCommentsRes = new GetCommentsRes();
        getCommentsRes.totalHits = comments.size();
        Set<String> userIds = new HashSet<String>();
        Set<String> entityIds = new HashSet<String>();
        for (Comment comment : comments) {
            userIds.add(comment.userId);
            entityIds.add(comment._getStringId());
            GetCommentRes cRes = new GetCommentRes(comment._getStringId(), comment.upVotes,
                    comment.views, comment.followers, comment.comments, comment.timeCreated,
                    comment.lastUpdated, false, comment.content, comment.parent, comment.type,
                    comment.hasMedia, comment.base, comment.root, comment.scope, comment.tags);
            cRes.user = new UserInfo(comment.userId, null, null, null, null);
            cRes.addImageSrcUrl();
            getCommentsRes.list.add(cRes);
        }
        annotateUserSocialActionInfos(orgId, userId, EntityType.COMMENT, getCommentsRes.list,
                userIds, entityIds);
        return getCommentsRes;
    }


    protected  void annotateUserSocialActionInfos(String orgId, String userId,
                                                        EntityType entityType, List<? extends AbstractContentUserActionRes> entites,
                                                        Set<String> userIds, Set<String> entityIds) {

        Map<String, ModelBasicInfo> userInfoMap = getUserInfoMap(orgId, userIds);
        Map<String, Boolean> entityVoteMap = getEntityUpVoteMap(
                userId, entityIds);
        Map<String, FollowType> followTypeMap = getEntityFollowTypeMap(userId, entityType, entityIds);
        for (AbstractContentUserActionRes res : entites) {
            String usrId = res.user.id;
            addSocialActionInfo(userId, usrId, entityVoteMap, followTypeMap, res);
            res.user = (UserInfo) userInfoMap.get(usrId);
        }
    }
    public Map<String, Boolean> getEntityUpVoteMap(String userId, Set<String> ids) {

        Map<String, Boolean> userActionMap = new HashMap<String, Boolean>();
        if (StringUtils.isEmpty(userId) || CollectionUtils.isEmpty(ids)) {
            return userActionMap;
        }


        List<EntityUserActionMapping> results = entityUserActionMappingRepo.findByUserIdAndTargetIn(userId,ids);

        for (EntityUserActionMapping e : results) {
            userActionMap.put(e.target.id, true);
        }
        return userActionMap;
    }
    public Map<String, FollowType> getEntityFollowTypeMap(String userId, EntityType entityType,
                                                          Set<String> ids) {

        Map<String, FollowType> userEntityFollowTypeMap = new HashMap<String, FollowType>();
        if (StringUtils.isEmpty(userId) || CollectionUtils.isEmpty(ids)) {
            return userEntityFollowTypeMap;
        }

        List<EntityUserActionMapping> results = entityUserActionMappingRepo.findAllByUserIdAndActionTypeAndTargetIdIn(userId,UserActionType.FOLLOWING.name(),ids);

        Set<String> followingEntityIds = new HashSet<String>();
        for (EntityUserActionMapping eMapping : results) {
            followingEntityIds.add(eMapping.target.id);
        }

        Set<String> followerEntityIds = new HashSet<String>();

        if (entityType == EntityType.USER) {
            // this block ensure addition of followType, entityType=USER
            List<EntityUserActionMapping> rsp = entityUserActionMappingRepo.findAllByUserIdInAndTargetId(ids,UserActionType.FOLLOWING.name(),userId);

            for (EntityUserActionMapping eMapping : rsp) {
                followerEntityIds.add(eMapping.userId);
            }
        }

        for (String id : ids) {
            boolean isFollowing = followingEntityIds.contains(id);
            boolean isFollower = followerEntityIds.contains(id);
            FollowType followType = userId.equals(id) ? FollowType.YOU : (isFollowing
                    && isFollower ? FollowType.BOTH_WAYS : (isFollowing ? FollowType.FOLLOWING
                    : (isFollower ? FollowType.FOLLOWER : FollowType.NONE)));
            userEntityFollowTypeMap.put(id, followType);
        }
        return userEntityFollowTypeMap;
    }

    public Map<String, ModelBasicInfo> getUserInfoMap(String orgId,
                                                      Collection<String> userIds, boolean excludeOrgMappingInfo) {

        logger.info("getUserInfoMap orgId:" + orgId + ", userIds: " + userIds);
        if (CollectionUtils.isEmpty(userIds)) {
            return new HashMap<String, ModelBasicInfo>();
        }
        Query query = new Query();
        Criteria criteria = new Criteria();

        boolean isOrgReq = !StringUtils.isEmpty(orgId);

        if (isOrgReq) {
            criteria.and(ConstantsGlobal.ORG_ID).is(orgId);
            criteria.and(ConstantsGlobal.USER_ID).in(userIds);

        } else {
            criteria.and(ConstantsGlobal._ID).in(userIds);
        }
        query.addCriteria(criteria);
        List<OrgMember> orgMemberList = mongoTemplate.find(query, OrgMember.class);
        List<User> users = mongoTemplate.find(query, User.class);

        Map<String, ModelBasicInfo> userIdToBasicInfoMap = isOrgReq ? populateOrgMemberInfo(
                orgMemberList, excludeOrgMappingInfo) : toBasicUserInfosMap(users);

        logger.debug("userIds map : " + userIdToBasicInfoMap);
        return userIdToBasicInfoMap;

    }

    public Comment addComment(SrcEntity base, SrcEntity root, SrcEntity parent,
                              Scope scope, String userId, String content, Set<String> tags,
                              CommentType type) {

        Comment comment = new Comment(userId, content, parent, type, base,
                root, scope, tags);
        logger.debug("saving comment : " + comment);
        commentRepo.save(comment);
        return comment;
    }

    public void markDoubtAsSolved(String discussionId, String teacherUserId) {

        DoubtTransaction transaction = doubtTransactionRepo.findByDiscussionId(discussionId);
        if (!transaction.completed) {
            transaction.completed = true;
            transaction.completedAt = System.currentTimeMillis();
            transaction.completedBy = teacherUserId;
            transaction.state = DoubtState.COMPLETED;
            doubtTransactionRepo.save(transaction);
        }
        cleanCurrentAssignedDoubt(teacherUserId);
    }

    public TeacherAnalytics cleanCurrentAssignedDoubt(String teacherId) {
        TeacherAnalytics teacher = teacherAnalyticsRepo.findByTeacherOrgMemberId(teacherId);
        if (null == teacher) {
            logger.error("cannot find teacher for teacherId: " + teacherId);
            return null;
        }
        teacher.currentAssaignedDoubt = "";
        teacherAnalyticsRepo.save(teacher);
        return teacher;
    }

}
