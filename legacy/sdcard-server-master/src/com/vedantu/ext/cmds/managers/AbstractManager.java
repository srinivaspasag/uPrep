package com.vedantu.ext.cmds.managers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import com.vedantu.ext.cmds.utils.commons.StringUtils;
import com.vedantu.ext.cmds.utils.config.ErrorMessageUtils;
import com.vedantu.ext.cmds.web.VedantuHttpResponse;

public abstract class AbstractManager {

    protected static final String FIELD_ADDED_AFTER = "addedAfter";
    protected Logger              LOGGER;

    public AbstractManager() {

        super();
        LOGGER = Logger.getLogger(getClass());
    }

    protected void checkForErrorResponse(VedantuHttpResponse webRes) throws ServletException {

        if (webRes.responseCode != HttpStatus.SC_OK) {
            throw new ServletException("Error Status: " + webRes.responseCode);
        }
        if (StringUtils.isNotEmpty(webRes.errorCode)) {
            LOGGER.error(webRes.errorCode);
            throw new ServletException(ErrorMessageUtils.getErrorMessage(webRes.errorCode));
        }

    }

    protected String getSyncKey(String prefix, List<String> inputs) {

        LOGGER.debug("Input array" + inputs);
        List<String> tempList = new ArrayList<String>(inputs);
        tempList.add(0, prefix);
        return StringUtils.join("/", tempList);
    }
}
