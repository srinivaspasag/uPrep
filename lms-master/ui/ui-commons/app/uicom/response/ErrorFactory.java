package uicom.response;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import play.Logger;

public class ErrorFactory {

    private static ErrorFactory INSTANCE = new ErrorFactory();

    public static final String SERVICE_ERROR = "SERVICE_ERROR";

    private Map<Class, Set<String>> errors;

    private ErrorFactory() {
        errors = new HashMap<Class, Set<String>>();
    }

    public static ErrorFactory getInstance() {
        return INSTANCE;
    }

    public void register(Class application, String errorCode) {
        if (!errors.containsKey(application)) {
            errors.put(application, new HashSet<String>());
        }

        Set<String> appErrors = errors.get(application);
        if (appErrors.contains(errorCode)) {
            Logger.log4j.warn("duplicate error insert request : application = "
                    + application + ", errorCode = " + errorCode);
        } else {
            appErrors.add(errorCode);
        }
    }

    public ErrorInfo getErrorInfo(Class application, String errorCode) {
        return getErrorInfo(application, errorCode, null);
    }

    public ErrorInfo getErrorInfo(Class application, String errorCode,
            String errorMessage) {
        if (StringUtils.isEmpty(errorCode) || !errors.containsKey(application)
                || !errors.get(application).contains(errorCode)) {
            Logger.log4j
                    .error("unidentified errorCode requested - application:"
                            + application + ", errorCode:" + errorCode
                            + ", errorMessage:" + errorMessage);
            errorCode = SERVICE_ERROR;
        }
        return new ErrorInfo(errorCode, errorMessage);
    }
}
