package com.lms.services.serviceImpl;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.utils.SessionExtractorUtils;
import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.SrcType;
import com.lms.managers.AbstractContentManager;
import com.lms.models.Documents;
import com.lms.pojos.requests.GetDocumentReq;
import com.lms.pojos.requests.GetDocumentsReq;
import com.lms.pojos.requests.GetSimilarEntities;
import com.lms.pojos.responce.GetDocumentRes;
import com.lms.pojos.responce.SearchListResponse;
import com.lms.pojos.search.details.AbstractFileModelIndexSearchDetails;
import com.lms.pojos.search.details.DocumentSearchIndexDetails;
import com.lms.repository.DocumentsRepo;
import com.lms.services.DocumentsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DocumentsServiceImpl extends AbstractContentManager implements DocumentsService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentsServiceImpl.class);
    @Autowired
    private SessionExtractorUtils sessionExtractorUtils;
    @Autowired
    private DocumentsRepo documentsRepo;
    @Autowired
    private DiscussionsServiceImpl discussionsServiceImpl;
    @Value("${application.session.cookie}")
    private String applicationSessionCookie;


    @Override
    public VedantuResponse getdocument(GetDocumentReq getDocumentReq) {
        if (getDocumentReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        //TODO:Need to implement
        // Map<String, String> sessionParamsMap = getSessionParams();
        // getDocumentReq.__setSessionParams(sessionParamsMap);

        GetDocumentRes response = get(getDocumentReq);

        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse getdocuments(GetDocumentsReq getDocumentsReq) {
        if (getDocumentsReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        SearchListResponse<GetDocumentRes> getVideosRes = gets(getDocumentsReq);

        return new VedantuResponse(getVideosRes);
    }

    @Override
    public VedantuResponse getsimilarDocuments(GetSimilarEntities getSimilarEntities) {
        if (getSimilarEntities.entity == null || getSimilarEntities.entity.id == null || getSimilarEntities.entity.type == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        ListResponse<GetDocumentRes> response = getSimilarDocuments(getSimilarEntities);
        return new VedantuResponse(response);
    }

    protected Map<String, String> getSessionParams() {

        //return sessionExtractorUtils.getSessionParams(applicationSessionCookie));
        return null;
    }

    public GetDocumentRes get(GetDocumentReq request) throws VedantuException {

        Optional<Documents> document1 = documentsRepo.findById(request.id);
        if (!document1.isPresent()) {
            throw new VedantuException(VedantuErrorCode.DOCUMENT_NOT_FOUND);
        }
        Documents document = document1.get();
        if (document.recordState == VedantuRecordState.TEMPORARY) {
            throw new VedantuException(VedantuErrorCode.CONTENT_IN_TEMPORARY_STAGE);
        }
        GetDocumentRes documentRes = new GetDocumentRes();
        documentRes.fromMongoModel(document);
        logger.info("document Info: " + documentRes);
        documentRes = (GetDocumentRes) discussionsServiceImpl.annotateExtraInfo(
                request.userId,
                document.contentSrc != null && document.contentSrc.type == EntityType.ORGANIZATION ? document.contentSrc.id
                        : null, EntityType.DOCUMENT, documentRes);

        annotateDocumentURLInfo(documentRes, request.isWebReq(), request.__getSessionParams(), request.orgId);
        return documentRes;
    }

    private void annotateDocumentURLInfo(DocumentSearchIndexDetails document, boolean isWebReq,
                                         Map<String, String> sessionParams, String orgId) {

        annotateLinkInfo(document);
        if (document.linkType == SrcType.LinkType.UPLOADED) {
            //For UPrep Organisation.
            if (!StringUtils.isEmpty(orgId) && orgId.equals("5df8a0d0e4b0897459b25d86")) {
                // document.url = ImageDisplayURLUtil.getEntityDocumentURL(EntityType.DOCUMENT, document.uuid, document.extension, FileCategory.CONVERTED);
            } else {
             /*   document.url = (document.converted) ? ImageDisplayURLUtil.getEntityDocumentSecureURL(
                        EntityType.DOCUMENT, document.uuid, sessionParams, isWebReq)
                        : ImageDisplayURLUtil.getEntityDocumentSecureURL(EntityType.DOCUMENT,
                        document.uuid, document.extension, FileCategory.CONVERTED,
                        sessionParams, isWebReq);*/
            }
        }

        if (!StringUtils.isEmpty(document.thumbnail)) {
            // document.thumbnail = ImageDisplayURLUtil.getEntityThumbnail(EntityType.DOCUMENT, document.thumbnail);
        } else {
            document.thumbnail = HardCodedConstants.emptyString;
        }
    }

    protected void annotateLinkInfo(AbstractFileModelIndexSearchDetails model) {

        if (model.linkInfo != null) {
            model.linkInfo.populate();
        }
    }

    public SearchListResponse<GetDocumentRes> gets(GetDocumentsReq request) throws VedantuException {

        SearchListResponse<GetDocumentRes> results = discussionsServiceImpl.getEntityInfos(request, EntityType.DOCUMENT,
                GetDocumentRes.class, null);
        discussionsServiceImpl.annotateExtraInfo(request.userId, request.orgId, EntityType.DOCUMENT, results.list);
        annotateDocumentURLInfo(results.list, request.isWebReq(), request.orgId);
        return results;
    }

    private void annotateDocumentURLInfo(List<? extends DocumentSearchIndexDetails> documentsList,
                                         boolean isWebReq, String orgId) {

        for (DocumentSearchIndexDetails document : documentsList) {
            annotateDocumentURLInfo(document, isWebReq, orgId);
        }
    }

    private void annotateDocumentURLInfo(DocumentSearchIndexDetails document, boolean isWebReq, String orgId) {

        annotateDocumentURLInfo(document, isWebReq, null, orgId);
    }

    public ListResponse<GetDocumentRes> getSimilarDocuments(GetSimilarEntities request) {

        ListResponse<GetDocumentRes> results = getSimilarEntityInfos(request, GetDocumentRes.class,
                null);
        discussionsServiceImpl.annotateExtraInfo(request.userId, request.orgId, EntityType.DOCUMENT, results.list);
        //  annotateDocumentURLInfo(results.list, request.isWebReq(), request.orgId);
        return results;
    }


}
