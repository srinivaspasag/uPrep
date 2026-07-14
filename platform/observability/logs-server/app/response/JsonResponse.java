package response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import play.libs.Json;

/**
 * The standard Json Response
 * Created by Raghu Teja on 27-06-2017.
 */
public class JsonResponse {
    public static final String PAGE_NOT_FOUND = "Page Not Found";

    private Object result;
    private String errorMessage;
    private String errorCode;

    public JsonResponse(Object result, String errorMessage, String errorCode) {

        super();
        this.result = null != result ? result : StringUtils.EMPTY;
        this.errorMessage = null != errorMessage ? errorMessage : StringUtils.EMPTY;
        this.errorCode = null != errorCode ? errorCode : StringUtils.EMPTY;
    }

    public JsonResponse(Object result) {

        this(result, StringUtils.EMPTY, StringUtils.EMPTY);
    }

    public Object getResult() {

        return result;
    }

    public void setResult(Object result) {

        this.result = result;
    }

    public String getErrorMessage() {

        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {

        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {

        return errorCode;
    }

    public void setErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }

    public ObjectNode toObjectNode() {

        ObjectNode o = Json.newObject();
        o.set("result", Json.toJson(result));
        o.put("errorMessage", errorMessage);
        o.put("errorCode", errorCode);
        return o;
    }
}
