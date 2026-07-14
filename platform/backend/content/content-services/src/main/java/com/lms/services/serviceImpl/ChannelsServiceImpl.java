package com.lms.services.serviceImpl;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.pojos.ContentLinkRelationshipDetails;
import com.lms.common.pojos.ScheduleInfo;
import com.lms.common.utils.ObjectIdUtils;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.enums.EncryptionLevel;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.models.Channel;
import com.lms.models.LibraryContentLink;
import com.lms.pojo.request.AbstractOrgScopeReq;
import com.lms.pojos.requests.AddChannelReq;
import com.lms.pojos.requests.AddContentToChannelReq;
import com.lms.pojos.requests.EditChannelReq;
import com.lms.pojos.requests.GetChannelsReq;
import com.lms.pojos.responce.AddChannelRes;
import com.lms.pojos.responce.AddContentToChannelRes;
import com.lms.pojos.responce.GetChannelRes;
import com.lms.repository.ChannelsRepo;
import com.lms.repository.LibraryContentLinksRepo;
import com.lms.services.ChannelsService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ChannelsServiceImpl implements ChannelsService {
    private static final Logger logger = LoggerFactory.getLogger(ChannelsServiceImpl.class);

    @Autowired
    ChannelsRepo channelsRepo;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    LibraryContentLinksRepo libraryContentLinksRepo;

//    @Autowired
//    AbstractContentManager abstractContentManager;

    @Override
    public VedantuResponse addChannel(AddChannelReq addChannelReq) {
        validateOrgScopeReq(addChannelReq);
        Channel channel = addChannel(addChannelReq.userId, addChannelReq.name,
                new SrcEntity(EntityType.ORGANIZATION, addChannelReq.orgId), addChannelReq.scope);
        AddChannelRes addChannelRes = new AddChannelRes(channel._getStringId(), channel.name);
        return new VedantuResponse(addChannelRes);
    }

    private static void validateOrgScopeReq(AbstractOrgScopeReq req) throws VedantuException {

        if ((req.orgId).isEmpty()) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "orgId is missing");
        }
    }
    public Channel addChannel(String userId, String name, SrcEntity contentSrc, Scope scope)
            throws VedantuException
    {
        Criteria criteria=new Criteria();
        Query query=new Query();
        criteria.and(ConstantsGlobal.USER_ID).is(userId);
        criteria.and(ConstantsGlobal.NAME).is(name);
        criteria.and(ConstantsGlobal.CONTENT_SRC).is(contentSrc);
        query.addCriteria(criteria);
        Channel channel = mongoTemplate.findOne(query, Channel.class);
        if (channel != null)
        {
            String errorMsg = "a channel with name:" + name + ", already exist for this : "
                    + contentSrc.type;
            logger.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.CHANNEL_ALREADY_EXIST, errorMsg);
        }
        channel = new Channel(userId, name, contentSrc, scope);
        channelsRepo.save(channel);
        return channel;
    }

    @Override
    public VedantuResponse getChannels(GetChannelsReq getChannelsReq) {

        if(getChannelsReq==null)
        {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        Criteria criteria=new Criteria();
        Query query=new Query();
        criteria.and(ConstantsGlobal.CONTENT_SRC).is(new SrcEntity(
                EntityType.ORGANIZATION, getChannelsReq.orgId));
        query.addCriteria(criteria).with(Sort.by(Sort.Direction.valueOf(getChannelsReq.sortOrder),getChannelsReq.orderBy));
        List<Channel> channelList = mongoTemplate.find(query, Channel.class);
        ListResponse<GetChannelRes> getChannelsRes = new ListResponse<GetChannelRes>();
        getChannelsRes.totalHits = channelList.size();
        for (Channel channel : channelList) {
            GetChannelRes channelRes = new GetChannelRes(channel._getStringId(), channel.name,
                    channel.contentCount);
            getChannelsRes.list.add(channelRes);
        }
        return new VedantuResponse(getChannelsRes);
    }

    @Override
    public VedantuResponse editChannel(EditChannelReq editChannelReq) {
        if (editChannelReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        validateOrgScopeReq(editChannelReq);
        Channel channel = getChannel(editChannelReq.getId());
        channel.name = editChannelReq.name;
        if (editChannelReq.scope != null) {
            channel.scope = editChannelReq.scope;
        }
        channelsRepo.save(channel);
        AddChannelRes addChannelRes = new AddChannelRes(channel._getStringId(), channel.name);
        return new VedantuResponse(addChannelRes);
    }

    public Channel getChannel(String id) throws VedantuException {

        Channel channel = channelsRepo.findById(id).get();
        if (channel == null) {
            throw new VedantuException(VedantuErrorCode.CHANNEL_NOT_FOUND);
        }
        return channel;
    }


    @Override
    public VedantuResponse addContentToChannel(AddContentToChannelReq addReq, boolean addOnIndexing) {
        Channel channel = getChannel(addReq.id);

        UserActionType actionType = UserActionType.ADDED;
        LibraryContentLink link = addLink(addReq.entity,
                new SrcEntity(EntityType.CHANNEL, channel._getStringId()), actionType,
                addReq.userId, Scope.PUBLIC);
        AddContentToChannelRes addRes = new AddContentToChannelRes();
        addRes.processed = link != null;
        incContentCount(addReq.id);
        if (!addOnIndexing) {
            ContentLinkRelationshipDetails channelEntityDetails = new ContentLinkRelationshipDetails(
                    addReq.userId, addReq.entity, new SrcEntity(EntityType.CHANNEL, addReq.id),
                    Scope.PUBLIC);
//            abstractContentManager.updateUserActionMappintToEs(channelEntityDetails, addReq.entity, actionType,
//                    UserActionType.EventActionType.ADD, null);
        }
        return new VedantuResponse(addRes);
    }

    public boolean incContentCount(String id) {
        String FIELD_ID = "_id";

        if (ObjectIdUtils.hasInvalidId(id)) {
            return false;
        }
        Criteria criteria = new Criteria();
        Query query = new Query();
        Update update = new Update();
        criteria.and(FIELD_ID).is(new ObjectId(id));
        update.set("contentCount", 1);
        query.addCriteria(criteria);
        mongoTemplate.updateFirst(query, update, Channel.class);
        return true;
    }


    public LibraryContentLink addLink(SrcEntity content, SrcEntity targetEntity,
                                      UserActionType linkType, String actorId, Scope scope) throws VedantuException {

        return addLink(content, targetEntity, linkType, actorId, scope, null);
    }

    public LibraryContentLink addLink(SrcEntity content, SrcEntity targetEntity,
                                      UserActionType linkType, String actorId, Scope scope, ScheduleInfo scheduleInfo)
            throws VedantuException {

        return addLink(content, targetEntity, linkType, actorId, scope, null, null);
    }

    public LibraryContentLink addLink(SrcEntity content, SrcEntity targetEntity,
                                      UserActionType linkType, String actorId, Scope updatedScope, ScheduleInfo scheduleInfo,
                                      Boolean downloadble) throws VedantuException {

        return addLink(content, targetEntity, linkType, actorId, updatedScope, scheduleInfo,
                downloadble, EncryptionLevel.NA, false);
    }

    /* TODO Added by Shivank */
    public LibraryContentLink addLink(SrcEntity content, SrcEntity targetEntity,
                                      UserActionType linkType, String actorId, Scope updatedScope, ScheduleInfo scheduleInfo,
                                      Boolean downloadble, EncryptionLevel encLevel, boolean allowDuplicates)
            throws VedantuException {

        return addLink(content, targetEntity, linkType, actorId, updatedScope, scheduleInfo,
                downloadble, encLevel, allowDuplicates, null, -1);
    }

    /**
     * It will add active link to library
     *
     * @param content
     * @param targetEntity
     * @param linkType
     * @param actorId
     * @param updatedScope
     * @param scheduleInfo
     * @param downloadble
     * @param encLevel
     * @return
     * @throws VedantuException
     */
    public LibraryContentLink addLink(SrcEntity content, SrcEntity targetEntity,
                                      UserActionType linkType, String actorId, Scope updatedScope, ScheduleInfo scheduleInfo,
                                      Boolean downloadble, EncryptionLevel encLevel, boolean allowDuplicates,
                                      List<SrcEntity> downloadableEntities, long position) throws VedantuException {

        logger.debug("...................Inside function add link..............");

        AtomicLong totalHits = new AtomicLong(0L);
        List<LibraryContentLink> links = getLibraryContentLinks(content, targetEntity, linkType,
                actorId, VedantuRecordState.ACTIVE, 0, 1, totalHits);

        if (totalHits.longValue() > 1 && !allowDuplicates) {
            logger.error("content:" + content + ", already added to  targetEntity:" + targetEntity);
            throw new VedantuException(VedantuErrorCode.ALREADY_ADDED, "content:" + content
                    + ", already added to  targetEntity:" + targetEntity);

        }

        LibraryContentLink contentLinkage = null;

        if (!links.isEmpty() || links.size() != 0) {
            contentLinkage = links.get(0);
            logger.debug("Updating  content Link" + contentLinkage);

        } else {
            logger.debug("Creating new content Link");
            contentLinkage = new LibraryContentLink(targetEntity, content);

        }
        if (position != -1) {
            contentLinkage.position = position;
        }
        if (scheduleInfo != null) {
            contentLinkage.setSchedule(scheduleInfo);
        }
        if (actorId != null) {
            contentLinkage.userId = actorId;
        }
        if (linkType != null) {
            contentLinkage.linkType = linkType;
        }
        if (updatedScope != null && updatedScope != Scope.UNKNOWN) {
            logger.debug("New scope" + updatedScope);
            contentLinkage.setScope(updatedScope);
        }
        if (downloadble != null) {
            logger.debug("New downloadable state" + downloadble);

            if (contentLinkage.getScope() == Scope.PRIVATE && downloadble) {
                logger.debug("Can not make downloadable" + downloadble);
                throw new VedantuException(VedantuErrorCode.CONTENT_NOT_VISIBLE, "content:"
                        + content + ",is not visible" + targetEntity);
            }
            contentLinkage.setDownloadable(downloadble);
        }
        if (encLevel != null
                && (contentLinkage.getEncLevel() != EncryptionLevel.NA || encLevel != EncryptionLevel.NA)) {
            contentLinkage.setEncLevel(encLevel);
        }

        if (content.type == EntityType.MODULE) {
            contentLinkage.setDownloadableEntities(downloadableEntities);
        }

        logger.debug("Saving content link in ILE library"
                + contentLinkage.getDownloadableEntities());
        libraryContentLinksRepo.save(contentLinkage);

        return contentLinkage;
    }

    public List<LibraryContentLink> getLibraryContentLinks(SrcEntity content,
                                                           SrcEntity targetEntity, UserActionType linkType, String actorId,
                                                           VedantuRecordState recordState, int start, int size, AtomicLong totalHits) {
        Criteria criteria = new Criteria();
        Query query = new Query();
        if (targetEntity != null) {
            if (targetEntity.type != null) {
                criteria.and("target.type").is(targetEntity.type);
                if (targetEntity.id != null) {
                    criteria.and("target.id").is(targetEntity.id);
                }
            }
        }
        if (content != null) {
            if (content.type != null) {
                criteria.and("source.type").is(content.getType());
                if (content.id != null) {
                    criteria.and("source.id").is(content.getId());
                }
            }

        }
        if (linkType != null) {
            criteria.and("linkType").is(linkType);
        }

        logger.debug("Querying for " + LibraryContentLink.class);

        if (recordState != null) {
            criteria.and("recordState").is(recordState);
        }
        query.addCriteria(criteria);
        logger.debug("Query: " + query.toString());
        List<LibraryContentLink> libraryLinks = mongoTemplate.find(query, LibraryContentLink.class);
        if (totalHits != null) {
            long count = libraryLinks.size();
            totalHits.set(count);
            logger.debug("Total matched results " + totalHits);
        }

        return libraryLinks;
    }
}
