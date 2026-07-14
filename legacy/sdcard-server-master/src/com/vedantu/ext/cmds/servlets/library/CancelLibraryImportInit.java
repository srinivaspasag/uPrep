package com.vedantu.ext.cmds.servlets.library;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;
import org.json.JSONObject;

import com.vedantu.ext.cmds.db.datamanagers.ImportedLibraryDataManager;
import com.vedantu.ext.cmds.db.models.ImportedLibrary;
import com.vedantu.ext.cmds.enums.DownloadProcessState;
import com.vedantu.ext.cmds.servlets.AbstractVedantuServlet;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;

@WebServlet("/cancelImportLibrary")
public class CancelLibraryImportInit extends AbstractVedantuServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        super.doPost(req, resp);

        ImportedLibrary library = ImportedLibraryDataManager.INSTANCE.getSyncedLibrary(org._id,
                (String) httpParams.get(ConstantGlobal.ID));
        LOGGER.debug("Library" + library.getName());
        library.state = DownloadProcessState.CANCELLED.name();
        ImportedLibraryDataManager.INSTANCE.update(library, library.STATE);

        JSONObject wRes = new JSONObject();
        wRes.put("sectionId", (String) httpParams.get(ConstantGlobal.ID));
        wRes.put("cancelled", true);
        resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        resp.getWriter().append(wRes.toString());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {

        doPost(req, resp);
    }
}
