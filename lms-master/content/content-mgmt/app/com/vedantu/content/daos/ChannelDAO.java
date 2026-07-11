package com.vedantu.content.daos;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.UpdateOperations;
import com.google.code.morphia.query.UpdateResults;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.content.models.Channel;
import com.vedantu.mongo.VedantuBasicDAO;

public class ChannelDAO extends VedantuBasicDAO<Channel, ObjectId> {

    private static final ALogger   LOGGER   = Logger.of(ChannelDAO.class);

    public static final ChannelDAO INSTANCE = new ChannelDAO();

    private ChannelDAO() {

        super(Channel.class);
    }

    public Channel addChannel(String userId, String name, SrcEntity contentSrc, Scope scope)
            throws VedantuException {

        Channel channel = getQuery().filter(ConstantsGlobal.USER_ID, userId)
                .filter(ConstantsGlobal.NAME, name).filter(ConstantsGlobal.CONTENT_SRC, contentSrc)
                .get();
        if (channel != null) {
            String errorMsg = "a channel with name:" + name + ", already exist for this : "
                    + contentSrc.type;
            LOGGER.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.CHANNEL_ALREADY_EXIST, errorMsg);
        }
        channel = new Channel(userId, name, contentSrc, scope);
        save(channel);
        return channel;
    }

    public Channel getChannel(String id) throws VedantuException {

        Channel channel = getById(id);
        if (channel == null) {
            throw new VedantuException(VedantuErrorCode.CHANNEL_NOT_FOUND);
        }
        return channel;
    }

    public boolean incContentCount(String id) {

        if (ObjectIdUtils.hasInvalidId(id)) {
            return false;
        }
        UpdateOperations<Channel> update = createUpdateOperations().inc("contentCount", 1);
        UpdateResults<Channel> updateResult = update(getQuery().filter(FIELD_ID, new ObjectId(id)),
                update);
        return !updateResult.getHadError();
    }

}
