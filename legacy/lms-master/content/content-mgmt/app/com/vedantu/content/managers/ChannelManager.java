package com.vedantu.content.managers;

import org.apache.commons.lang3.StringUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.commons.relationships.ContentLinkRelationshipDetails;
import com.vedantu.content.daos.ChannelDAO;
import com.vedantu.content.daos.LibraryContentLinksDAO;
import com.vedantu.content.models.Channel;
import com.vedantu.content.models.LibraryContentLink;
import com.vedantu.content.pojos.requests.channels.AddChannelReq;
import com.vedantu.content.pojos.requests.channels.AddContentToChannelReq;
import com.vedantu.content.pojos.requests.channels.EditChannelReq;
import com.vedantu.content.pojos.requests.channels.GetChannelsReq;
import com.vedantu.content.pojos.responses.channels.AddChannelRes;
import com.vedantu.content.pojos.responses.channels.AddContentToChannelRes;
import com.vedantu.content.pojos.responses.channels.GetChannelRes;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class ChannelManager extends AbstractContentManager {

    public static AddChannelRes addChannel(AddChannelReq addChannelReq) throws VedantuException {

        validateOrgScopeReq(addChannelReq);
        Channel channel = ChannelDAO.INSTANCE.addChannel(addChannelReq.userId, addChannelReq.name,
                new SrcEntity(EntityType.ORGANIZATION, addChannelReq.orgId), addChannelReq.scope);
        AddChannelRes addChannelRes = new AddChannelRes(channel._getStringId(), channel.name);
        return addChannelRes;
    }

    public static AddChannelRes updateChannel(EditChannelReq editChannelReq)
            throws VedantuException {

        validateOrgScopeReq(editChannelReq);
        Channel channel = ChannelDAO.INSTANCE.getChannel(editChannelReq.id);
        channel.name = editChannelReq.name;
        if (editChannelReq.scope != null) {
            channel.scope = editChannelReq.scope;
        }
        ChannelDAO.INSTANCE.save(channel);
        AddChannelRes addChannelRes = new AddChannelRes(channel._getStringId(), channel.name);
        return addChannelRes;
    }

    public static ListResponse<GetChannelRes> getChannels(GetChannelsReq getChannelsReq)
            throws VedantuException {

        DBObject query = new BasicDBObject(ConstantsGlobal.CONTENT_SRC, new SrcEntity(
                EntityType.ORGANIZATION, getChannelsReq.orgId).toDBObject());
        VedantuDBResult<Channel> channels = ChannelDAO.INSTANCE.getInfos(query, null,
                getChannelsReq.start, getChannelsReq.size,
                MongoManager.getSortQuery(getChannelsReq.orderBy, getChannelsReq.sortOrder));
        ListResponse<GetChannelRes> getChannelsRes = new ListResponse<GetChannelRes>();
        getChannelsRes.totalHits = channels.totalHits;
        for (Channel channel : channels.results) {
            GetChannelRes channelRes = new GetChannelRes(channel._getStringId(), channel.name,
                    channel.contentCount);
            getChannelsRes.list.add(channelRes);
        }
        return getChannelsRes;
    }

    public static AddContentToChannelRes addContentToChannel(AddContentToChannelReq addReq,
            boolean addOnIndexing) throws VedantuException {

        Channel channel = ChannelDAO.INSTANCE.getChannel(addReq.id);

        UserActionType actionType = UserActionType.ADDED;
        LibraryContentLink link = LibraryContentLinksDAO.INSTANCE.addLink(addReq.entity,
                new SrcEntity(EntityType.CHANNEL, channel._getStringId()), actionType,
                addReq.userId, Scope.PUBLIC);
        AddContentToChannelRes addRes = new AddContentToChannelRes();
        addRes.processed = link != null;
        ChannelDAO.INSTANCE.incContentCount(addReq.id);
        if (!addOnIndexing) {
            ContentLinkRelationshipDetails channelEntityDetails = new ContentLinkRelationshipDetails(
                    addReq.userId, addReq.entity, new SrcEntity(EntityType.CHANNEL, addReq.id),
                    Scope.PUBLIC);
            updateUserActionMappintToEs(channelEntityDetails, addReq.entity, actionType,
                    EventActionType.ADD, null);
        }
        return addRes;
    }

    private static void validateOrgScopeReq(AbstractOrgScopeReq req) throws VedantuException {

        if (StringUtils.isEmpty(req.orgId)) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "orgId is missing");
        }
    }

}
