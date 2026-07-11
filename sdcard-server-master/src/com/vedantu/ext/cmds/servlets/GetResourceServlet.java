package com.vedantu.ext.cmds.servlets;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.vedantu.ext.cmds.db.datamanagers.ResourceDataManager;
import com.vedantu.ext.cmds.db.models.Resource;
import com.vedantu.ext.cmds.managers.ResourceManager;
import com.vedantu.ext.cmds.pojo.responses.GetResourcesRes;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;

@WebServlet("/resources")
public class GetResourceServlet extends AbstractVedantuServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {

        super.doGet(req, resp);
        final String targetId = req.getParameter(ConstantGlobal.TARGET_ID);
        final String targetType = req.getParameter(ConstantGlobal.TARGET_TYPE);

        LOGGER.debug("targetId: " + targetId + " targetType:" + targetType);

        boolean remoteOnly = Boolean.parseBoolean(req.getParameter("remoteOnly"));

        GetResourcesRes remoteRes = ResourceManager.INSTANCE.getRemoteResources(org, targetId,
                targetType, httpParams);
        if (remoteOnly) {
            printJSONResponse(resp, remoteRes.toJSON());
            return;
        }

        List<Resource> resources = ResourceDataManager.INSTANCE.getResources(targetId, targetType,
                null);
        // req.setAttribute("resources", resources);
        //
        // req.getServletContext().getRequestDispatcher("/resources.jsp").forward(req, resp);
        JSONArray wRes = new JSONArray(resources);
        JSONObject jsonRes = new JSONObject();
        jsonRes.put("resources", wRes);
        printJSONResponse(resp, jsonRes);
        // 1st check if the Config.DESKTOP_FOLDER_NAME is created

    }
}
