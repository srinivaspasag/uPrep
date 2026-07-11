package com.lms.services.serviceImpl;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.utils.ImageDisplayURLUtil;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.entity.storage.FileCategory;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.OperationType;
import com.lms.common.vedantu.mongo.EntityOperationStatus;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.component.CMDSLibraryManager;
import com.lms.component.CmdsDocumentManager;
import com.lms.enums.SrcType;
import com.lms.managers.AbstractContentManager;
import com.lms.models.CMDSDocument;
import com.lms.models.event.search.details.CMDSDocumentSearchIndexDetails;
import com.lms.pojos.requests.GetCMDSDocumentReq;
import com.lms.pojos.responce.GetCMDSDocumentRes;
import com.lms.repo.CMDSDocumentRepo;
import com.lms.services.CmdsDocumentsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class CmdsDocumentsServiceImpl extends AbstractContentManager implements CmdsDocumentsService {

    private static final Logger logger = LoggerFactory.getLogger(CmdsDocumentsServiceImpl.class);
    protected static final String RECORD_STATE = "recordState";
    protected static final String FIELD_ID     = "_id";




    @Autowired
    private CMDSDocumentRepo cmdsDocumentRepo;
    @Autowired
    private CmdsDocumentManager cmdsDocumentManager;
    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public VedantuResponse reconvert(GetCMDSDocumentReq request) {
        CMDSDocument cmdsDocument = null;
        if (StringUtils.isEmpty(request.orgId)) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }

        if (StringUtils.isEmpty(request.userId)) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }

        cmdsDocument = cmdsDocumentRepo.findById(request.id).get();

        CMDSDocument document = cmdsDocumentRepo.findById(request.id).get();
        if (document == null) {
            throw new VedantuException(VedantuErrorCode.DOCUMENT_NOT_FOUND);
        }

        GetCMDSDocumentRes response = new GetCMDSDocumentRes();
        response.fromMongoModel(cmdsDocument);
        annotateExtraInfo(request.userId, request.orgId, EntityType.CMDSDOCUMENT, response);

        annotateDocumentURLInfo(response, request.isWebReq());

        if (cmdsDocument.linkType == SrcType.LinkType.UPLOADED) {
            if (response.operationJobIdMap == null) {
                response.operationJobIdMap = new HashMap<OperationType, String>();
            }
            cmdsDocumentManager.startProcessingUploadDoc(cmdsDocument, response.operationJobIdMap);
        }
        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse get(GetCMDSDocumentReq request) {
        CMDSDocument document = cmdsDocumentRepo.findById(request.id).get();
        if (document == null) {
            throw new VedantuException(VedantuErrorCode.DOCUMENT_NOT_FOUND);
        }

        GetCMDSDocumentRes getDocumentRes = new GetCMDSDocumentRes();
        getDocumentRes.fromMongoModel(document);

        annotateExtraInfo(request.userId, request.orgId, EntityType.CMDSDOCUMENT, getDocumentRes);
        annotateDocumentURLInfo(getDocumentRes, request.isWebReq(), request.__getSessionParams());

        EntityOperationStatus status = getByOType(EntityType.CMDSDOCUMENT, document._getStringId(),
                        OperationType.DOCUMENT_CONVERSION);
        if (status != null && status.recordState == VedantuRecordState.ACTIVE) {
            if (status.numOfSteps != 0 && status.numOfSteps == status.numOfStepsCompleted) {
                updateState(status, VedantuRecordState.DELETED);
            } else {
                getDocumentRes.operationJobIdMap.put(OperationType.DOCUMENT_CONVERSION,
                        status._getStringId());
            }
        }
        return new VedantuResponse(getDocumentRes);
    }

    public EntityOperationStatus getByOType(EntityType type, String id, OperationType oType) {
        Criteria criteria=new Criteria();
        Query query=new Query();
        criteria.and("id").is(id);
        criteria.and("type").is(type);
        criteria.and("oType").is(oType);
        query.addCriteria(criteria);
        return mongoTemplate.findOne(query,EntityOperationStatus.class);
    }
    public boolean updateState(EntityOperationStatus entity, VedantuRecordState state) {

        logger.debug("Updated entity :" + entity + " new state " + state + EntityOperationStatus.class + " T"
                + entity.getClass());
        try {
            Query query = new Query();
            Criteria criteria = new Criteria();
            criteria.and(FIELD_ID).is(entity.id);
            Update update = new Update();
            update.set(RECORD_STATE, state);
            query.addCriteria(criteria);
            logger.debug("Updated entity :" + entity + " with query " + query.toString());
            mongoTemplate.updateMulti(query, update, EntityOperationStatus.class);
            logger.debug("Updated recordstate to :" + state);
            entity.recordState = state;
            return true;
        }
        catch(Exception e) {
            logger.error("Unable to update recordstate to :" + state);
            return false;
        }
    }

    private void annotateDocumentURLInfo(CMDSDocumentSearchIndexDetails document, boolean isWebReq) {

        annotateDocumentURLInfo(document, isWebReq, null);
    }

    private void annotateDocumentURLInfo(CMDSDocumentSearchIndexDetails document, boolean isWebReq,
                                         Map<String, String> sessionParams) {

        if (document.linkType == SrcType.LinkType.UPLOADED) {
            document.url = (document.converted) ? ImageDisplayURLUtil.getEntityDocumentSecureURL(
                    EntityType.CMDSDOCUMENT, document.uuid, sessionParams, isWebReq)
                    : ImageDisplayURLUtil.getEntityDocumentSecureURL(EntityType.CMDSDOCUMENT,
                    document.uuid, document.extension, FileCategory.CONVERTED,
                    sessionParams, isWebReq);
        }

        if (!StringUtils.isEmpty(document.thumbnail)) {
            document.thumbnail = ImageDisplayURLUtil.getEntityThumbnail(EntityType.CMDSDOCUMENT,
                    document.thumbnail);
        } else {
            document.thumbnail = "";
        }

    }
}
