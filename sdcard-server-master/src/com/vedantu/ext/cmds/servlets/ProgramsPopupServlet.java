package com.vedantu.ext.cmds.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vedantu.ext.cmds.utils.web.WebCommunicator;
import com.vedantu.ext.cmds.web.ReqAction;
import com.vedantu.ext.cmds.web.VedantuHttpResponse;
@WebServlet("/programsPopup")
public class ProgramsPopupServlet extends AbstractVedantuServlet {

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
        VedantuHttpResponse webRes = WebCommunicator.getResult(ReqAction.GET_PROGRAMS, httpParams);
        // printJSONResponse(resp, webRes.toJSON());
        req.setAttribute("webRes", webRes);
        req.setAttribute("targetEntityType", "PROGRAM");
        req.getRequestDispatcher("/programsPopup.jsp").forward(req, resp);
    }

}
