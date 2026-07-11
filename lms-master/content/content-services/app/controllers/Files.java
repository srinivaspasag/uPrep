package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.commons.pojos.responses.SearchListResponse;
import com.vedantu.content.managers.FileManager;
import com.vedantu.content.pojos.requests.GetSimilarEntities;
import com.vedantu.content.pojos.requests.file.GetFileReq;
import com.vedantu.content.pojos.requests.file.GetFilesReq;
import com.vedantu.content.pojos.responses.files.GetFileRes;

public class Files extends AbstractVedantuController {

    private static final ALogger LOGGER = Logger.of(Files.class);

    public static Result get() {

        Form<GetFileReq> requestForm = Form.form(GetFileReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        GetFileReq request = requestForm.get();
        GetFileRes response = null;
        try {
            response = FileManager.INSTANCE.get(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result gets() {

        Form<GetFilesReq> requestForm = Form.form(GetFilesReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        GetFilesReq request = requestForm.get();
        SearchListResponse<GetFileRes> response = null;
        try {
            response = FileManager.INSTANCE.gets(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result getSimilarFiles() {

        Form<GetSimilarEntities> requestForm = Form.form(GetSimilarEntities.class)
                .bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, requestForm.errors()
                            .toString())).toObjectNode());
        }
        GetSimilarEntities request = requestForm.get();
        ListResponse<GetFileRes> response = FileManager.INSTANCE.getSimilarFiles(request);
        return ok(getResultResponse(response).toObjectNode());
    }

}
