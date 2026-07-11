package com.vedantu.ext.cmds.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.vedantu.ext.cmds.db.datamanagers.OrgDataManager;
import com.vedantu.ext.cmds.db.models.Organization;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.utils.commons.StringUtils;
import com.vedantu.ext.cmds.utils.config.ErrorMessageUtils;
import com.vedantu.ext.cmds.web.VedantuHttpResponse;

public abstract class AbstractVedantuServlet extends HttpServlet {

    /**
	 * 
	 */
    private static final long     serialVersionUID   = 1L;

    protected final String        FIELD_ORDER_BY     = "orderBy";
    protected final String        FIELD_SORT_ORDER   = "sortOrder";
    protected final String        NO_GET_REQ_ALLOWED = "NO_GET_REQ_ALLOWED";
    protected HttpSession         session;
    protected Logger              LOGGER;
    protected Map<String, Object> httpParams;

    protected String[]            reqParamsList;
    protected Organization        org;

    public AbstractVedantuServlet() {

        super();
        LOGGER = Logger.getLogger(getClass());
        org = OrgDataManager.INSTANCE.getOrganization();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {

        session = req.getSession();
        populateHttpParams(req);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        session = req.getSession();
        populateHttpParams(req);
        logReqParams(req);
    }

    protected void printJSONResponse(HttpServletResponse resp, JSONObject jsonAware)
            throws IOException {

        resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        PrintWriter pw = resp.getWriter();
        pw.append(jsonAware.toString());
    }

    protected void checkForErrorResponse(VedantuHttpResponse webRes) throws ServletException {

        if (webRes.responseCode != HttpStatus.SC_OK) {
            throw new ServletException("Error Status: " + webRes.responseCode + ", errorMessage:"
                    + webRes.errorMessage);
        }
        if (StringUtils.isNotEmpty(webRes.errorCode)) {
            LOGGER.error(webRes.errorCode);
            throw new ServletException(ErrorMessageUtils.getErrorMessage(webRes.errorCode));
        }

    }

    protected boolean foundErrorInResponseAndRedirecToPage(HttpServletRequest req,
            HttpServletResponse resp, VedantuHttpResponse webRes, String jspPath)
            throws ServletException, IOException {

        boolean foundError = false;
        if (webRes.responseCode != HttpStatus.SC_OK) {
            resp.sendRedirect("500.jsp");
            foundError = true;
        }
        if (StringUtils.isNotEmpty(webRes.errorCode)) {
            LOGGER.error("Error Code found in Response:" + webRes.errorCode);
            foundError = true;
            redirectToPage(resp, jspPath, "errorMessage",
                    ErrorMessageUtils.getErrorMessage(webRes.errorCode));
        }
        return foundError;
    }

    protected void redirectToPage(HttpServletResponse resp, String jspPath, String cookieName,
            String cookieValue) throws ServletException, IOException {

        redirectToPage(resp, jspPath, Arrays.asList(cookieName), Arrays.asList(cookieValue));
    }

    protected void redirectToPage(HttpServletResponse resp, String jspPath,
            List<String> cookieNames, List<String> cookieValues) throws ServletException,
            IOException {

        if (cookieNames != null && cookieValues != null
                && cookieNames.size() == cookieValues.size()) {
            for (int i = 0; i < cookieNames.size(); i++) {
                Cookie cookie = new Cookie(cookieNames.get(i), cookieValues.get(i));
                LOGGER.info("setting the error message cookie, message:" + cookieValues.get(i));
                resp.addCookie(cookie);
            }
        }
        resp.sendRedirect(jspPath);
    }

    protected void redirectToNextPage(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (StringUtils.isNotEmpty(req.getParameter("next"))) {
            resp.sendRedirect(URLDecoder.decode(req.getParameter("next"), "UTF-8"));
        } else {
            resp.sendRedirect("landing.jsp");
        }
    }

    private void populateHttpParams(HttpServletRequest req) throws ServletException {

        if (org == null) {
            org = OrgDataManager.INSTANCE.getOrganization();
        }
        httpParams = new HashMap<String, Object>();
        Enumeration<String> keys = req.getParameterNames();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            httpParams.put(key, req.getParameter(key));
        }
        if (org != null) {
            httpParams.put(ConstantGlobal.ORG_ID, org.id);
            httpParams.put(ConstantGlobal.USER_ID, org.adminUserId);
        }

        Enumeration<String> sKeys = session.getAttributeNames();

        while (sKeys.hasMoreElements()) {
            String key = sKeys.nextElement();
            httpParams.put(key, session.getAttribute(key));
        }

        if (reqParamsList != null) {
            List<String> missingParams = new ArrayList<String>();
            for (String key : reqParamsList) {
                if (httpParams.get(key) == null
                        || StringUtils.isEmpty(httpParams.get(key).toString())) {
                    missingParams.add(key);
                }
            }

            if (!missingParams.isEmpty()) {
                throw new ServletException("missing required paramaters : " + missingParams);
            }
        }
    }

    protected void logReqParams(HttpServletRequest req) {

        LOGGER.debug("request params : " + req.getParameterMap());
    }

    public String getMacAddress() {

        try {

            Enumeration<NetworkInterface> it = NetworkInterface.getNetworkInterfaces();
            if (it.hasMoreElements()) {
                NetworkInterface ni1 = it.nextElement();
                if (ni1 != null) {
                    byte[] mac = ni1.getHardwareAddress();
                    if (mac != null) {
                        String macAdd = "";
                        for (int k = 0; k < mac.length; k++) {
                            macAdd += String.format("%02X%s", mac[k], (k < mac.length - 1) ? "-"
                                    : "");
                        }
                        return macAdd;
                    } else {
                        System.out.println("Address doesn't exist ");
                    }
                } else {
                    System.out.println("address is not found.");
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

}
