package controllers;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Logger.ALogger;
import play.Play;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.Controller;

import com.vedantu.commons.JSONResponse;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.SessionExtractorUtils;

/**
 * All Vedantu services should extend {@link AbstractVedantuController}.
 * 
 * @author ujjawal
 * 
 */
public abstract class AbstractVedantuController extends Controller {

    private static final ALogger LOGGER = Logger.of(AbstractVedantuController.class);

    protected static JSONResponse getErrorResponse(VedantuException e) {

        LOGGER.error("sending error rsp : " + e.getMessage() + " errorCode " + e.errorCode, e);
        return new JSONResponse(e);
    }

    protected static JSONResponse getErrorResponse(VedantuException e, Object result) {

        LOGGER.error("sending error rsp : " + e.getMessage() + " errorCode " + e.errorCode
                + " result " + result, e);
        return new JSONResponse(e, result);
    }

    protected static JSONResponse getResultResponse(Object result) {

        LOGGER.debug("response: " + result);
        if (result instanceof ListResponse<?>) {
            return getResultResponse((ListResponse<?>) result);
        }
        return new JSONResponse(result);
    }

    protected static JSONResponse getResultResponse(ListResponse<?> result) {

        LOGGER.debug("response for list response: " + result);
        if (result.cumulativeErrorCode != null) {
//            return new JSONResponse(result, result.cumulativeErrorCode.getMessage(),
//                    result.cumulativeErrorCode.errorCode.name());
        }
        return new JSONResponse(result);
    }

    protected static String getErrorMessege(Form<?> form) {

        StringBuilder sb = new StringBuilder();
        boolean start = true;
        for (List<ValidationError> errors : form.errors().values()) {
            for (ValidationError error : errors) {
                if (!start) {
                    sb.append(", ");
                }
                start = false;
                sb.append(error.key());
                sb.append(":");
                sb.append(error.message());
            }
        }
        return sb.toString();
    }

    protected static void deleteFile(String fileName, File file) {

        FileUtils.deleteFile(fileName, file);
    }

    protected static Map<String, Object> getReqParams() {

        Map<String, String[]> reqParams = new HashMap<String, String[]>();
        reqParams.putAll(request().queryString());

        Map<String, String[]> reqBodyParams = request().body().asFormUrlEncoded();
        if (reqBodyParams != null) {
            reqParams.putAll(reqBodyParams);
        }

        Map<String, Object> allParams = new HashMap<String, Object>();
        if (null != reqParams && !reqParams.isEmpty()) {
            StringBuilder sb = new StringBuilder("reqParams : {");
            boolean isFirst = true;
            for (Map.Entry<String, String[]> entry : reqParams.entrySet()) {
                if (!isFirst) {
                    sb.append(", ");
                }
                sb.append(entry.getKey()).append("=[")
                        .append(StringUtils.join(entry.getValue(), ",")).append("]");
                isFirst = false;
            }
            sb.append("}");
            // Logger.log4j.info(sb.toString());
            for (Entry<String, String[]> entry : reqParams.entrySet()) {
                List<String> value = null != entry.getValue() ? Arrays.asList(entry.getValue())
                        : null;
                if (null != value) {
                    allParams.put(entry.getKey(), value.size() == 1 ? value.get(0) : value);
                }

            }
        }
        return allParams;
    }

    protected static Map<String, String> getSessionParams() {

        return SessionExtractorUtils.getSessionParams(request().cookie(
                Play.application().configuration().getString("application.session.cookie")));
    }
}
