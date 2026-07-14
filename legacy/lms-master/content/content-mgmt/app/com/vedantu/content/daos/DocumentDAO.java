package com.vedantu.content.daos;

import java.util.List;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.content.interfaces.IDownloadable;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.content.models.Document;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.user.daos.AbstractUserActionDAO;

public class DocumentDAO extends AbstractUserActionDAO<Document, ObjectId> implements IDownloadable {

    public static final DocumentDAO INSTANCE = new DocumentDAO();
    private static final ALogger    LOGGER   = Logger.of(DocumentDAO.class);

    public DocumentDAO() {

        super(Document.class);
        // TODO Auto-generated constructor stub
    }

    public Document getDocument(String id) throws VedantuException {

        Document document = getById(id);
        if (document == null) {
            LOGGER.error("no question found with id:" + id);
            throw new VedantuException(VedantuErrorCode.DOCUMENT_NOT_FOUND,
                    "no question found with id:" + id);
        }
        return document;
    }

    @Override
    public String getDownloadName(String id, VedantuBaseMongoModel record) {

        Document currentRecord = null;

        if (record == null) {
            currentRecord = getById(id);
        } else {
            if (record instanceof Document) {
                currentRecord = (Document) record;
            }
        }

        return FileUtils.getFileName(currentRecord.originalFileName);
    }

    @Override
    public List<SrcEntity> getChildren(String id) {

        // TODO Auto-generated method stub
        return null;
    }

    public Document getByCMDSDocId(String id) {
        Query<Document> query = getQuery().filter("cmdsDocId", id);
        Document doc = findOne(query);
        return doc;
    }


}
