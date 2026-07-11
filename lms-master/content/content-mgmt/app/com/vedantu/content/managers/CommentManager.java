package com.vedantu.content.managers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.relationships.EntityUserActionRelationshipSearchDetails;
import com.vedantu.content.daos.CommentDAO;
import com.vedantu.content.models.Comment;
import com.vedantu.content.pojos.requests.comments.AddCommentReq;
import com.vedantu.content.pojos.requests.comments.GetCommentReq;
import com.vedantu.content.pojos.requests.comments.GetCommentsReq;
import com.vedantu.content.pojos.responses.comments.AddCommentRes;
import com.vedantu.content.pojos.responses.comments.GetCommentRes;
import com.vedantu.content.pojos.responses.comments.GetCommentsRes;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.enums.OrgMemberProfile;
import com.vedantu.organization.models.OrgMember;
import com.vedantu.user.models.EntityUserActionMapping;
import com.vedantu.user.pojos.EntityUserActionDAO;
import com.vedantu.user.pojos.UserInfo;
import com.vedantu.user.social.actions.event.details.CommentDetails;

public class CommentManager extends AbstractContentManager {

    private static final ALogger LOGGER = Logger.of(CommentManager.class);

    public static AddCommentRes addComment(AddCommentReq addCommentReq) throws VedantuException {

        isSocialActionAllowed(addCommentReq.parent.type, addCommentReq.parent.id);
        try {
            addCommentReq.removeImageSrc(true);
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        Comment comment = CommentDAO.INSTANCE.addComment(addCommentReq.base, addCommentReq.root,
                addCommentReq.parent, addCommentReq.scope, addCommentReq.userId,
                addCommentReq.content, addCommentReq.tags != null ? new HashSet<String>(
                        addCommentReq.tags) : new HashSet<String>(), addCommentReq.type);

        updateParentCommentsCount(comment.userId, comment.parent);

        EntityUserActionMapping actionMapping = EntityUserActionDAO.INSTANCE
                .addEntityUserActionMapping(comment.userId, UserActionType.COMMENTED,
                        addCommentReq.parent, true);

        if (addCommentReq.parent.type != EntityType.COMMENT
                && addCommentReq.parent.type != EntityType.SOLUTION) {
            EntityUserActionRelationshipSearchDetails userActionDetails = new EntityUserActionRelationshipSearchDetails(
                    addCommentReq.userId, addCommentReq.parent.id);
            updateUserActionMappintToEs(userActionDetails, addCommentReq.parent,
                    UserActionType.COMMENTED, EventActionType.ADD, null);
        }
        //
        // CommentDetails details = new CommentDetails(addCommentReq.userId, EventType.ADD_COMMENT,
        // new SrcEntity(EntityType.COMMENT, comment._getStringId()),
        // actionMapping._getStringId(), comment._getStringId());

        CommentDetails details = new CommentDetails(addCommentReq.userId, EventType.ADD_COMMENT,
                addCommentReq.parent, actionMapping._getStringId(), comment._getStringId());

        details.commentText = comment.content;
        generateEventAysc(addCommentReq.userId, details, EventType.ADD_COMMENT);

        OrgMember member = OrgMemberDAO.INSTANCE.getByUserId(addCommentReq.userId);
        if(member.profile == OrgMemberProfile.TEACHER){
            DiscussionManager.markDoubtAsSolved(addCommentReq.parent.id, member._getStringId());
        }

        return annotateUserActionData(comment.contentSrc != null
                && comment.contentSrc.type == EntityType.ORGANIZATION ? comment.contentSrc.id
                : null, comment);
    }

    public static GetCommentRes getComment(GetCommentReq getCommentReq) throws VedantuException {

        Comment comment = CommentDAO.INSTANCE.getComment(getCommentReq.id);
        return annotateUserActionData(comment.contentSrc != null
                && comment.contentSrc.type == EntityType.ORGANIZATION ? comment.contentSrc.id
                : null, comment);
    }

    public static GetCommentsRes getComments(GetCommentsReq getCommentsReq) throws VedantuException {

        if (!getCommentsReq.validateRequestParams()) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        VedantuDBResult<Comment> comments = CommentDAO.INSTANCE.getComments(getCommentsReq.parent,
                getCommentsReq.start, getCommentsReq.size, getCommentsReq.orderBy,
                getCommentsReq.sortOrder);
        LOGGER.debug("getComments response: " + comments);
        return annotateUserActionData(null, getCommentsReq.userId, comments);
    }

    private static GetCommentsRes annotateUserActionData(String orgId, String userId,
            VedantuDBResult<Comment> comments) {

        GetCommentsRes getCommentsRes = new GetCommentsRes();
        getCommentsRes.totalHits = comments.totalHits;
        Set<String> userIds = new HashSet<String>();
        Set<String> entityIds = new HashSet<String>();
        for (Comment comment : comments.results) {
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

    private static AddCommentRes annotateUserActionData(String orgId, Comment comment) {

        AddCommentRes addCommentRes = new AddCommentRes(comment._getStringId(), comment.upVotes,
                comment.views, comment.followers, comment.comments, comment.timeCreated,
                comment.lastUpdated, false, comment.content, comment.parent, comment.type,
                comment.hasMedia, comment.base, comment.root, comment.scope, comment.tags);
        addCommentRes.user = (UserInfo) getUserInfoMap(orgId, Arrays.asList(comment.userId)).get(
                comment.userId);
        addCommentRes.addImageSrcUrl();
        return addCommentRes;
    }

    public static VedantuBaseMongoModel getParent(Comment comment) {

        if (comment.parent == null || comment.parent.id == null) {
            return null;
        }
        return comment.parent.get();
    }

    public static VedantuBaseMongoModel getRoot(Comment comment) {

        if (comment.root == null || comment.root.id == null) {
            return null;
        }
        return comment.root.get();
    }

}
