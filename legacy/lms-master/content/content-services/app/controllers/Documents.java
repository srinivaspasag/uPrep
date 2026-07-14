package controllers;

import java.util.Map;

import play.data.Form;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.commons.pojos.responses.SearchListResponse;
import com.vedantu.content.managers.DocumentManager;
import com.vedantu.content.pojos.requests.GetSimilarEntities;
import com.vedantu.content.pojos.requests.documents.GetDocumentReq;
import com.vedantu.content.pojos.requests.documents.GetDocumentsReq;
import com.vedantu.content.pojos.responses.documents.GetDocumentRes;

public class Documents extends AbstractVedantuController {

    // private static final ALogger LOGGER = Logger.of(Documents.class);

    public static Result get() {

        Form<GetDocumentReq> requestForm = Form.form(GetDocumentReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }

        GetDocumentReq request = requestForm.get();
        Map<String, String> sessionParamsMap = getSessionParams();
        request.__setSessionParams(sessionParamsMap);

        GetDocumentRes response = null;

        try {
            response = DocumentManager.INSTANCE.get(request);

        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result gets() {

        Form<GetDocumentsReq> requestForm = Form.form(GetDocumentsReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        GetDocumentsReq request = requestForm.get();
        SearchListResponse<GetDocumentRes> getVideosRes = null;
        try {
            getVideosRes = DocumentManager.INSTANCE.gets(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getVideosRes).toObjectNode());
    }

    public static Result getSimilarDocs() {

        Form<GetSimilarEntities> requestForm = Form.form(GetSimilarEntities.class)
                .bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, requestForm.errors()
                            .toString())).toObjectNode());
        }
        GetSimilarEntities request = requestForm.get();
        ListResponse<GetDocumentRes> response = DocumentManager.INSTANCE
                .getSimilarDocuments(request);
        return ok(getResultResponse(response).toObjectNode());
    }

}
