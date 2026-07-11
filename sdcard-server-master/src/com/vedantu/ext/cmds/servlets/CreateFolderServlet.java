package com.vedantu.ext.cmds.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.vedantu.ext.cmds.db.datamanagers.FolderDataManager;
import com.vedantu.ext.cmds.db.models.Folder;
import com.vedantu.ext.cmds.db.models.Organization;
import com.vedantu.ext.cmds.managers.DataManagerUtils;
import com.vedantu.ext.cmds.pojo.responses.CreateFolderRes;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.utils.web.WebCommunicator;
import com.vedantu.ext.cmds.web.ReqAction;
import com.vedantu.ext.cmds.web.VedantuHttpResponse;

@WebServlet("/createFolder")
public class CreateFolderServlet extends AbstractVedantuServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public CreateFolderServlet() {

        super();
        reqParamsList = new String[] { ConstantGlobal.NAME, "parentFolderId" };
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        super.doPost(req, resp);
        Organization org = DataManagerUtils.getOrganization();

        VedantuHttpResponse webRes = WebCommunicator.getResult(ReqAction.CREATE_FOLDER, httpParams);
        checkForErrorResponse(webRes);
        CreateFolderRes createFolderRes = new CreateFolderRes();
        webRes.populateResult(createFolderRes);
        Folder folder = new Folder(org._id, createFolderRes.id, createFolderRes.name,
                createFolderRes.parent, org.adminUserId);
        try {
            FolderDataManager.INSTANCE.insert(folder);
        } catch (Exception e) {
            resp.getWriter().println("can not create folder " + e.getMessage());
        }
        resp.getWriter().println(new JSONObject(folder));
    }

}
