package com.vedantu.ext.cmds.servlets;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;

import com.vedantu.ext.cmds.db.datamanagers.OrgDataManager;
import com.vedantu.ext.cmds.db.models.Organization;
import com.vedantu.ext.cmds.pojo.responses.GetOrgInfoRes;
import com.vedantu.ext.cmds.utils.commons.StringUtils;
import com.vedantu.ext.cmds.utils.config.Config;
import com.vedantu.ext.cmds.utils.config.ErrorMessageUtils;
import com.vedantu.ext.cmds.utils.web.WebCommunicator;
import com.vedantu.ext.cmds.web.ReqAction;
import com.vedantu.ext.cmds.web.VedantuHttpResponse;

@WebServlet("/addOrg")
public class AddOrgServlet extends AbstractVedantuServlet {

    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddOrgServlet() {

        super();
        reqParamsList = new String[] { "url" };
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {

        throw new ServletException(ErrorMessageUtils.getErrorMessage(NO_GET_REQ_ALLOWED));
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        super.doPost(req, resp);

        Organization org = OrgDataManager.INSTANCE.getOrganization();
        if (org != null) {
            // resp.sendError(HttpStatus.SC_BAD_REQUEST, "your system is already configured for "
            // + org.name);
            // return;
            throw new ServletException("Your system is already configured for " + org.name);
        }

        String urlParam = req.getParameter("url");

        String host = null;
        try {
            URL uri = new URL(urlParam);
            host = urlParam.substring(0, urlParam.indexOf(uri.getPath()));
            LOGGER.debug("host: " + host);
            if (StringUtils.isEmpty(host)) {
                throw new Exception("invalid host " + host);
            }

            String slug = urlParam.substring(urlParam.lastIndexOf("/") + 1);
            httpParams.put("slug", slug);
        } catch (Exception e) {
            throw new ServletException("url " + urlParam + " is not a valid url");
        }

        // 1. validate org slug
        httpParams.put("getKey", true);
        Config.REMOTE_HOST = host;

        VedantuHttpResponse webRes = WebCommunicator.getResult(ReqAction.GET_ORG_INFO, httpParams);
        if (foundErrorInResponseAndRedirecToPage(req, resp, webRes, "index.jsp")) {
            return;
        }

        GetOrgInfoRes getOrgResPojo = new GetOrgInfoRes();
        webRes.populateResult(getOrgResPojo);
        if (StringUtils.isEmpty(getOrgResPojo.id) || StringUtils.isEmpty(getOrgResPojo.name)) {
            throw new ServletException("No/Invalid Response received from server");
        }

        org = new Organization(0, getOrgResPojo.adminUserId, getOrgResPojo.name,
                getOrgResPojo.thumb, getOrgResPojo.id, getOrgResPojo.slug, getMacAddress(), null,
                null, getOrgResPojo.key, host);

        try {
            OrgDataManager.INSTANCE.insert(org);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            resp.sendError(HttpStatus.SC_BAD_REQUEST, e.getMessage());
        }
        redirectToNextPage(req, resp);
    }
}
