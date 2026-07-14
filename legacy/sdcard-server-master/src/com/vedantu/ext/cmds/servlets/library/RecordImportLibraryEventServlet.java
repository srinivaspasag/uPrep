package com.vedantu.ext.cmds.servlets.library;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.vedantu.ext.cmds.db.models.ImportedLibrary;
import com.vedantu.ext.cmds.managers.LibrarySyncManager;
import com.vedantu.ext.cmds.servlets.AbstractVedantuServlet;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;

@WebServlet("/recordImportLibrary")
public class RecordImportLibraryEventServlet extends AbstractVedantuServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        super.doPost(req, resp);
        logReqParams(req);
        LibrarySyncManager librarySyncManager = new LibrarySyncManager();
        LOGGER.debug("Starting library sync of size" + httpParams.get(ConstantGlobal.SIZE));
        ImportedLibrary importedLibrary = librarySyncManager.insertImportLibraryEvent(org._id,
                (String) httpParams.get(ConstantGlobal.ID),
                (String) httpParams.get(ConstantGlobal.TYPE),
                (String) httpParams.get(ConstantGlobal.NAME),
                Long.parseLong((String)httpParams.get(ConstantGlobal.SIZE)));

        JSONObject wRes = new JSONObject(importedLibrary);
        printJSONResponse(resp, wRes);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {

        doPost(req, resp);
    }
}
