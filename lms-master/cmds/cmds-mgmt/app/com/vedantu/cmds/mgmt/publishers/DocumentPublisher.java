package com.vedantu.cmds.mgmt.publishers;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.daos.CMDSDocumentDAO;
import com.vedantu.cmds.managers.AbstractCMDSContentManager;
import com.vedantu.cmds.models.CMDSDocument;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.ObjectMapperUtils;
import com.vedantu.content.daos.DocumentDAO;
import com.vedantu.content.models.Document;
import com.vedantu.content.pojos.SrcType.LinkType;
import com.vedantu.content.search.details.DocumentSearchIndexDetails;
import com.vedantu.mongo.VedantuBaseMongoModel;

public class DocumentPublisher extends AbstractCMDSContentManager {

    private static final ALogger          LOGGER   = Logger.of(DocumentPublisher.class);

    public static final DocumentPublisher INSTANCE = new DocumentPublisher();

    private DocumentPublisher() {

        super();
        EntityTypePublisherFactory.INSTANCE.register(EntityType.CMDSDOCUMENT, this);
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

        CMDSDocument cmdsDocument = CMDSDocumentDAO.INSTANCE.getById(content.id);
        // if (cmdsDocument.published) {
        //     throw new VedantuException(VedantuErrorCode.ALREADY_PUBLISHED);
        // }
        if (cmdsDocument.linkType == LinkType.ADDED && StringUtils.isEmpty(cmdsDocument.url)) {
            throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_PUBLISHED);
        }

        if (cmdsDocument.linkType == LinkType.UPLOADED
                && (StringUtils.isEmpty(cmdsDocument.uuid)|| StringUtils.isEmpty(cmdsDocument.thumbnail) || !cmdsDocument.converted/*
                                                               * TODO enforce conversion for web
                                                               * processing && cmdsVideo . converted
                                                               */)) {

            throw new VedantuException(VedantuErrorCode.NOT_CONVERTED);
        }

        // create new ILE video
        Document document;
        if(!StringUtils.isEmpty(cmdsDocument.globalDocId)){
            document = DocumentDAO.INSTANCE.getDocument(cmdsDocument.globalDocId);
        }else{

            document = ObjectMapperUtils.convertValue(cmdsDocument, Document.class);
        }
        // copy stuff

        document.setCMDSDocId(cmdsDocument._getStringId());
        document.description = cmdsDocument.description;
        document.extension = cmdsDocument.extension;
        document.uuid = cmdsDocument.uuid;
        document.thumbnail = cmdsDocument.thumbnail;
        document.linkType = cmdsDocument.linkType;
        document.url = cmdsDocument.url;
        document.scope = Scope.ORG;
        document.published = true;
        document.name = cmdsDocument.name;
        document.converted = cmdsDocument.converted;
        document.orientation= cmdsDocument.orientation;
        document.states = cmdsDocument.states;
        document.passphrase = cmdsDocument.passphrase;
        document.size = cmdsDocument.size;
        DocumentDAO.INSTANCE.save(document);

        // save new ILE video

        cmdsDocument.globalDocId = document._getStringId();
        cmdsDocument.published = document.published;
        cmdsDocument.publishingInProgress = false;
        CMDSDocumentDAO.INSTANCE.save(cmdsDocument);

        // live add global test to search index
        DocumentSearchIndexDetails details = new DocumentSearchIndexDetails();
        details.fromMongoModel(document);
        addLiveEntityToSearchIndex(details, EntityType.DOCUMENT, true);

        // create INDEX_VIDEO
        generateEventAysc(userId, cmdsDocument, EventActionType.UPDATE,
                EventType.INDEX_CMDS_DOCUMENT, UserActionType.UPDATED, false);

        return cmdsDocument;
    }

}
