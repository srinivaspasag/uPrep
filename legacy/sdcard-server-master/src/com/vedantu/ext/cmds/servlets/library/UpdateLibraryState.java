package com.vedantu.ext.cmds.servlets.library;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.vedantu.ext.cmds.db.datamanagers.ImportedLibraryDataManager;
import com.vedantu.ext.cmds.db.models.ImportedLibrary;
import com.vedantu.ext.cmds.enums.LibraryState;
import com.vedantu.ext.cmds.servlets.AbstractVedantuServlet;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.utils.db.FieldInfo;

@WebServlet("/updateLibrary")
public class UpdateLibraryState extends AbstractVedantuServlet {

    /**
     * 
     */

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        super.doPost(req, resp);
        logReqParams(req);
        String requestState = (String) httpParams.get(ImportedLibrary.STATE);

        String libraryId = (String) httpParams.get(ConstantGlobal.ID);
        if (LibraryState.DELETED.name().equals(requestState)) {
            try {
                ImportedLibraryDataManager.INSTANCE.delete(new FieldInfo(ImportedLibrary.ID,
                        libraryId));
            } catch (Exception e) {
                LOGGER.error("Failed to delete", e);
                throw new ServletException("Unable to update state");
            }
        } else {
            ImportedLibrary library = ImportedLibraryDataManager.INSTANCE.getSyncedLibrary(org._id,
                    libraryId);
            if (library != null) {
                library.state = LibraryState.DELETED.name();
                ImportedLibraryDataManager.INSTANCE.update(library, ImportedLibrary.STATE);
            }
        }
        JSONObject wRes = new JSONObject();
        wRes.put("success", true);
        printJSONResponse(resp, wRes);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {

        doPost(req, resp);
    }
}
