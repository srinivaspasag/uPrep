package com.vedantu.ext.cmds.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vedantu.ext.cmds.utils.web.WebCommunicator;
import com.vedantu.ext.cmds.web.ReqAction;
import com.vedantu.ext.cmds.web.VedantuHttpResponse;

@WebServlet("/acadEntities")
public class AcadEntitiesServlet extends AbstractVedantuServlet {

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
        ReqAction reqAction;

        if ("PROGRAM".equals(req.getParameter("entityType"))) {
            reqAction = ReqAction.GET_CENTERS_OF_PROGRAM;
            req.setAttribute("targetEntityType", "CENTER");
        } else {
            reqAction = ReqAction.GET_SECTIONS_OF_CENTER;
            req.setAttribute("targetEntityType", "SECTION");
        }
        VedantuHttpResponse webRes = WebCommunicator.getResult(reqAction, httpParams);
        req.setAttribute("webRes", webRes);
        req.getRequestDispatcher("/includes/acadEntities.jsp").forward(req, resp);
    }

}
