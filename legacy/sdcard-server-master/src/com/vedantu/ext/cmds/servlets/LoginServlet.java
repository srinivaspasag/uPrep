package com.vedantu.ext.cmds.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vedantu.ext.cmds.db.datamanagers.OrgDataManager;
import com.vedantu.ext.cmds.db.models.Organization;
import com.vedantu.ext.cmds.pojo.responses.AuthenticateRes;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.utils.commons.StringUtils;
import com.vedantu.ext.cmds.utils.config.ErrorMessageUtils;
import com.vedantu.ext.cmds.utils.web.WebCommunicator;
import com.vedantu.ext.cmds.web.ReqAction;
import com.vedantu.ext.cmds.web.VedantuHttpResponse;

@WebServlet("/logins")
public class LoginServlet extends AbstractVedantuServlet {

    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginServlet() {

        super();
        reqParamsList = new String[] { "username", "password" };
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
        logReqParams(req);

        String username = (String) httpParams.get(ConstantGlobal.USER_NAME);

        if (StringUtils.isValidEmail(username)) {
            httpParams.put("useGlobalUsername", true);
        } else {
            httpParams.put("useGlobalUsername", false);
            httpParams.put("memberId", username);
        }
        // 1. validate org slug
        VedantuHttpResponse webRes = WebCommunicator.getResult(ReqAction.AUTHENTICATE, httpParams);
        if (foundErrorInResponseAndRedirecToPage(req, resp, webRes, "index.jsp")) {
            return;
        }
        AuthenticateRes webResPojo = new AuthenticateRes();
        webRes.populateResult(webResPojo);

        Organization org = OrgDataManager.INSTANCE.getOrganization();
        if (org == null || !org.adminUserId.equals(webResPojo.id)) {
            // resp.sendError(HttpServletResponse.SC_UNAUTHORIZED,
            // "Only Super admin is allowed to login");
            // return;
            redirectToPage(resp, "index.jsp", "errorMessage",
                    "Only Super admin is allowed to login");
            return;
        }
        createSession(req, webResPojo, org);
        redirectToNextPage(req, resp);
    }

    private void
            createSession(HttpServletRequest req, AuthenticateRes werResPojo, Organization org) {

        session.setAttribute(ConstantGlobal.USER_ID, werResPojo.id);
        session.setAttribute("targetUserId", werResPojo.id);
        session.setAttribute("callingUserId", werResPojo.id);
        session.setAttribute("orgId", org.id);
        session.setAttribute("firstName", werResPojo.firstName);
        session.setAttribute("lastName", werResPojo.lastName);
    }

}
