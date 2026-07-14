package com.vedantu.cmds.mgmt.publishers;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.daos.CMDSFileDAO;
import com.vedantu.cmds.managers.AbstractCMDSContentManager;
import com.vedantu.cmds.models.CMDSFile;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.ObjectMapperUtils;
import com.vedantu.content.daos.FileDAO;
import com.vedantu.content.models.File;
import com.vedantu.content.pojos.SrcType.LinkType;
import com.vedantu.content.search.details.FileSearchIndexDetails;
import com.vedantu.mongo.VedantuBaseMongoModel;

public class FilePublisher extends AbstractCMDSContentManager {

    private static final ALogger      LOGGER   = Logger.of(FilePublisher.class);

    public static final FilePublisher INSTANCE = new FilePublisher();

    private FilePublisher() {

        super();
        EntityTypePublisherFactory.INSTANCE.register(EntityType.CMDSFILE, this);
    }

    @Override
    public void prePublish(SrcEntity content) {

        // TODO Auto-generated method stub

    }

    @Override
    public void postPublish(VedantuBaseMongoModel model) {

        // TODO Auto-generated method stub

    }

    @Override
    protected VedantuBaseMongoModel publish(String userId, String orgId, SrcEntity content)
            throws VedantuException {

        // get cmds video

        CMDSFile cmdsFile = CMDSFileDAO.INSTANCE.getById(content.id);
        // if (cmdsFile.published) {
        //     throw new VedantuException(VedantuErrorCode.ALREADY_PUBLISHED);
        // }
        if (cmdsFile.linkType == LinkType.ADDED && StringUtils.isEmpty(cmdsFile.url)) {
            throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_PUBLISHED);
        }

        if (cmdsFile.linkType == LinkType.UPLOADED
                && (StringUtils.isEmpty(cmdsFile.uuid) || !cmdsFile.converted)) {

            throw new VedantuException(VedantuErrorCode.NOT_CONVERTED);
        }

        // create new ILE video

        File publishedFile = ObjectMapperUtils.convertValue(cmdsFile, File.class);
        // copy stuff

        publishedFile.setCMDSFileId(cmdsFile._getStringId());
        publishedFile.description = cmdsFile.description;
        publishedFile.extension = cmdsFile.extension;
        publishedFile.uuid = cmdsFile.uuid;
        publishedFile.thumbnail = cmdsFile.thumbnail;
        publishedFile.linkType = cmdsFile.linkType;
        publishedFile.url = cmdsFile.url;
        publishedFile.scope = Scope.ORG;
        publishedFile.published = true;
        publishedFile.name = cmdsFile.name;
        publishedFile.states = cmdsFile.states;
        publishedFile.passphrase = cmdsFile.passphrase;
        publishedFile.size = cmdsFile.size;
        FileDAO.INSTANCE.save(publishedFile);

        // save new ILE video

        cmdsFile.globalFileId = publishedFile._getStringId();
        cmdsFile.published = publishedFile.published;
        CMDSFileDAO.INSTANCE.save(cmdsFile);

        // live add global test to search index
        FileSearchIndexDetails details = new FileSearchIndexDetails();
        details.fromMongoModel(publishedFile);
        addLiveEntityToSearchIndex(details, EntityType.FILE, true);

        // create INDEX_VIDEO
        generateEventAysc(userId, cmdsFile, EventActionType.UPDATE, EventType.INDEX_CMDS_FILE,
                UserActionType.UPDATED, false);

        return cmdsFile;
    }

}
