package com.vedantu.ext.cmds.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vedantu.ext.cmds.db.datamanagers.SDCardGroupDataManager;
import com.vedantu.ext.cmds.db.models.SDCardGroup;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;

@WebServlet("/sdCardGroups")
public class GetSDCardGroupServlet extends AbstractVedantuServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public GetSDCardGroupServlet() {

        super();
        reqParamsList = new String[] { "targetId", "targetType" };
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {

        super.doGet(req, resp);
        // add addedAfter flag
        // this will load new SDCardGroups from cmds server
        if (httpParams.get("sectionId") == null) {
            httpParams.put("sectionId", httpParams.get(ConstantGlobal.TARGET_ID));
        }

        String targetId = (String) httpParams.get(ConstantGlobal.TARGET_ID);
        final String targetType = "SECTION";

        List<SDCardGroup> sdCardGroups = SDCardGroupDataManager.INSTANCE.getSDCardGroups(org._id,
                targetId, targetType, (String) httpParams.get(FIELD_ORDER_BY),
                (String) httpParams.get(FIELD_SORT_ORDER));
        req.setAttribute("sdCardGroups", sdCardGroups);
        req.getRequestDispatcher("/sdCardGroups.jsp").forward(req, resp);
    }
}
