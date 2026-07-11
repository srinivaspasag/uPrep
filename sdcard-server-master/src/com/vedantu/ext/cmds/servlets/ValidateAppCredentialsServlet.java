package com.vedantu.ext.cmds.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vedantu.ext.cmds.db.datamanagers.FolderDataManager;
import com.vedantu.ext.cmds.db.datamanagers.OrgDataManager;
import com.vedantu.ext.cmds.db.models.Folder;
import com.vedantu.ext.cmds.db.models.Organization;
import com.vedantu.ext.cmds.pojo.responses.CreateFolderRes;
import com.vedantu.ext.cmds.pojo.responses.GetResourceRes;
import com.vedantu.ext.cmds.pojo.responses.GetResourcesRes;
import com.vedantu.ext.cmds.pojo.responses.ValidateOrgAppCredentialsRes;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.utils.config.Config;
import com.vedantu.ext.cmds.utils.config.ErrorMessageUtils;
import com.vedantu.ext.cmds.utils.web.WebCommunicator;
import com.vedantu.ext.cmds.web.ReqAction;
import com.vedantu.ext.cmds.web.VedantuHttpResponse;

@WebServlet("/validateAppCredentials")
public class ValidateAppCredentialsServlet extends AbstractVedantuServlet {

    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ValidateAppCredentialsServlet() {

        super();
        reqParamsList = new String[] { ConstantGlobal.ORG_ID, "authToken", "secretKey", "appId" };
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

        Organization org = OrgDataManager.INSTANCE.getOrganization(null,
                req.getParameter(ConstantGlobal.ORG_ID));
        if (org == null) {
            throw new ServletException("Invalid request paramaters");
        }

        VedantuHttpResponse webRes = WebCommunicator.getResult(
                ReqAction.VALIDATE_ORG_APP_CREDENTIALS, httpParams);
        if (foundErrorInResponseAndRedirecToPage(req, resp, webRes, "index.jsp")) {
            return;
        }

        ValidateOrgAppCredentialsRes validateAppRes = new ValidateOrgAppCredentialsRes();
        webRes.populateResult(validateAppRes);

        try {
            if (!validateAppRes.valid) {
                redirectToPage(resp, "index.jsp", "errorMessage",
                        "SecretKey and Authentication ID combination does not match");
                return;
            }
            org.authToken = req.getParameter(ConstantGlobal.AUTH_TOKEN);
            org.secretKey = req.getParameter(ConstantGlobal.SECRET_KEY);
            OrgDataManager.INSTANCE.update(org);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new ServletException(e.getMessage());
        }
        // after successful validation of org credentials, creat a desktop folder in the cmds root
        // folder
        httpParams.clear();
        httpParams.put(ConstantGlobal.NAME, Config.DESKTOP_FOLDER_NAME);
        webRes = WebCommunicator.getResult(ReqAction.CREATE_FOLDER, httpParams);
        Folder folder = null;
        try {
            checkForErrorResponse(webRes);
            CreateFolderRes createFolderRes = new CreateFolderRes();
            webRes.populateResult(createFolderRes);
            folder = new Folder(org._id, createFolderRes.id, createFolderRes.name,
                    createFolderRes.parent, org.adminUserId);
            FolderDataManager.INSTANCE.insert(folder);
        } catch (Exception e) {
            // smees the folder is alredy created
            if (webRes.errorCode.equals("FOLDER_ALREADY_EXISTS")) {
                // if the folder was already created in the server, created too it locally
                try {
                    folder = initFolders();
                } catch (Exception e1) {
                    LOGGER.error(e1.getMessage(), e1);
                }
            }
            LOGGER.error(e.getMessage(), e);
        }
        redirectToNextPage(req, resp);
    }

    private Folder initFolders() throws Exception {

        httpParams.clear();
        VedantuHttpResponse webRes = WebCommunicator.getResult(ReqAction.GET_FOLDERS, httpParams);
        checkForErrorResponse(webRes);

        GetResourcesRes listRes = new GetResourcesRes();

        webRes.populateResult(listRes);
        GetResourceRes rootFolder = listRes.list.get(0);
        // get all child folders and select folder with name Config.DESKTOP_FOLDER_NAME
        httpParams.clear();
        httpParams.put("folderId", rootFolder.id);
        httpParams.put(ConstantGlobal.ORG_ID, org.id);
        webRes = WebCommunicator.getResult(ReqAction.GET_FOLDERS, httpParams);
        listRes = new GetResourcesRes();
        webRes.populateResult(listRes);
        Folder desktopFolder = null;
        for (GetResourceRes res : listRes.list) {
            if (Config.DESKTOP_FOLDER_NAME.equalsIgnoreCase(res.name)) {
                desktopFolder = new Folder(org._id, res.id, res.name, rootFolder.id,
                        org.adminUserId);
                FolderDataManager.INSTANCE.insert(desktopFolder);
            }
        }
        return desktopFolder;
    }
}
