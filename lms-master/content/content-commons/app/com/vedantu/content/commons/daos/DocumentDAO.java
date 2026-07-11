package com.vedantu.content.commons.daos;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.content.models.Document;
import com.vedantu.content.models.Video;
import com.vedantu.mongo.VedantuBasicDAO;

public class DocumentDAO extends VedantuBasicDAO<Document, ObjectId>{

    private static final ALogger            LOGGER   = Logger.of(DocumentDAO.class);

    public static final DocumentDAO INSTANCE = new DocumentDAO();

    private DocumentDAO() {
        super(Document.class);
    }
//    public Document getDocument(String id) throws VedantuException {
//
//        Document document = getById(id);
//        if (document == null) {
//            LOGGER.error("no question found with id:" + id);
//            throw new VedantuException(VedantuErrorCode.DOCUMENT_NOT_FOUND,
//                    "no question found with id:" + id);
//        }
//        return document;
//    }

}
