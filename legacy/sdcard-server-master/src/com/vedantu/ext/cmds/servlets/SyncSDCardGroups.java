package com.vedantu.ext.cmds.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vedantu.ext.cmds.managers.SDCardsManager;
import com.vedantu.ext.cmds.pojo.responses.GetSDCardGroupsRes;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;

@WebServlet("/syncSDcardGroup")
public class SyncSDCardGroups extends AbstractVedantuServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {

        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        super.doPost(req, resp);

        if (httpParams.get("sectionId") == null) {
            httpParams.put("sectionId", httpParams.get(ConstantGlobal.TARGET_ID));
        }

        String targetId = (String) httpParams.get(ConstantGlobal.TARGET_ID);
        final String targetType = "SECTION";

        GetSDCardGroupsRes wRes = SDCardsManager.INSTANCE.getSDCardGroupsRes(org, httpParams,
                targetId, targetType);
        printJSONResponse(resp, wRes.toJSON());

    }

}
