package com.vedantu.ext.cmds.servlets.library;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;
import org.json.JSONArray;

import com.vedantu.ext.cmds.managers.LibraryManager;
import com.vedantu.ext.cmds.pojo.responses.local.GetImportedLibrariesRes;
import com.vedantu.ext.cmds.servlets.AbstractVedantuServlet;

@WebServlet("/getImportedLibraries")
public class GetImportedLibrariesServlet extends AbstractVedantuServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {

        super.doGet(req, resp);

        String state = (String) httpParams.get("state");

        LibraryManager manager = new LibraryManager();
        List<GetImportedLibrariesRes> syncedLibraries = manager.getLibraries(org._id, state);
        JSONArray res = new JSONArray(syncedLibraries);
        resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        resp.getWriter().append(res.toString());
    }
}
