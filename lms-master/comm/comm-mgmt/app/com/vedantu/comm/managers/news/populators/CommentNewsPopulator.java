package com.vedantu.comm.managers.news.populators;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.news.details.CommentNewsEntityDetails;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.enums.CommentType;
import com.vedantu.content.models.AbstractBoardEntityTagModel;
import com.vedantu.content.models.Comment;
import com.vedantu.mongo.IVedantuModel;
import com.vedantu.mongo.VedantuBaseMongoModel;

public class CommentNewsPopulator extends AbstractEntityDetailsPopulator {

    public final static CommentNewsPopulator INSTANCE = new CommentNewsPopulator();
    private final static ALogger             LOGGER   = Logger.of(CommentNewsPopulator.class);

    @Override
    public void populate(String orgId, String userId, Set<SrcEntity> newsEntities,
            Map<SrcEntity, SrcEntity> srcEntityDetails, EntityType entityType) {

        super.populate(orgId, userId, newsEntities, srcEntityDetails, entityType);
    }

    @Override
    public SrcEntity populate(String orgId, String userId, SrcEntity newsEntity,
            Map<String, IVedantuModel> modelDetailMap) {

        Comment comment = (Comment) modelDetailMap.get(newsEntity.id);
        if (comment == null) {
            LOGGER.error("no comment found for entity:" + newsEntity);
            return null;
        }
        CommentNewsEntityDetails commentDetails = new CommentNewsEntityDetails(newsEntity.type,
                newsEntity.id);
        commentDetails.content = comment.content;
        commentDetails.upVotes = comment.upVotes;
        commentDetails.isReply = comment.type == CommentType.REPLY;
        commentDetails.followers = comment.followers;
        commentDetails.comments = comment.comments;
        commentDetails.views = comment.views;
        commentDetails.timeCreated= comment.timeCreated;
        commentDetails.id = comment._getStringId();

        IPopulator populator = EntityDetailsPopulatorFactory.INSTANCE.get(comment.parent.type);
        if (populator == null) {
            LOGGER.error("no populator found for entityType: " + comment.parent.type);
            return null;
        }
        SrcEntity embededEntity = EntityNewsDetailsFactory.INSTANCE
                .getInstance(comment.parent.type);
        if (comment.parent != null && populator != null) {
            embededEntity.id = comment.parent.id;
            embededEntity.type = comment.parent.type;
            VedantuBaseMongoModel model = EntityTypeDAOFactory.INSTANCE.get(embededEntity.type)
                    .getById(embededEntity.id);
            Map<String, IVedantuModel> detailMap = new HashMap<String, IVedantuModel>();
            detailMap.put(model._getStringId(), model);
            embededEntity = populator.populate(orgId, userId, comment.parent, detailMap);

            commentDetails.parentDetails = embededEntity;
            if (model instanceof AbstractBoardEntityTagModel) {
                commentDetails.contentSrc = ((AbstractBoardEntityTagModel) model).contentSrc;
            }
        }

        if (!comment.root.equals(comment.parent)) {

            populator = EntityDetailsPopulatorFactory.INSTANCE.get(comment.root.type);
            if (populator == null) {
                LOGGER.error("no populator found for entityType: " + comment.root.type);
                return null;
            }
            embededEntity = EntityNewsDetailsFactory.INSTANCE.getInstance(comment.root.type);
            if (comment.parent != null && populator != null) {
                embededEntity.id = comment.root.id;
                embededEntity.type = comment.root.type;
                VedantuBaseMongoModel model = EntityTypeDAOFactory.INSTANCE.get(embededEntity.type)
                        .getById(embededEntity.id);
                Map<String, IVedantuModel> detailMap = new HashMap<String, IVedantuModel>();
                detailMap.put(model._getStringId(), model);

                embededEntity = populator.populate(orgId, userId, comment.parent, detailMap);

                commentDetails.rootDetails = embededEntity;
                if (model instanceof AbstractBoardEntityTagModel) {
                    commentDetails.contentSrc = ((AbstractBoardEntityTagModel) model).contentSrc;
                }
            }
        } else {
            commentDetails.rootDetails = commentDetails.parentDetails;
        }
        LOGGER.info("comment details " + commentDetails);
        return commentDetails;

    }
}
