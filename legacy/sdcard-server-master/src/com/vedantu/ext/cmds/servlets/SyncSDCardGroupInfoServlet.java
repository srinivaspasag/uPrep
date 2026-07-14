package com.vedantu.ext.cmds.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vedantu.ext.cmds.managers.SDCardsManager;
import com.vedantu.ext.cmds.pojo.responses.GetSDCardGroupInfoRes;

@WebServlet("/syncSDCardGroupInfo")
public class SyncSDCardGroupInfoServlet extends AbstractVedantuServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {

        super.doGet(req, resp);

        GetSDCardGroupInfoRes wRes = SDCardsManager.INSTANCE.getSDCardGroupInfo(org, httpParams);
        printJSONResponse(resp, wRes.toJSON());
    }
}
