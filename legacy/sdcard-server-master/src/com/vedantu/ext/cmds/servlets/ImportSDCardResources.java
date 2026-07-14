package com.vedantu.ext.cmds.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;
import org.json.JSONObject;

import com.vedantu.ext.cmds.db.datamanagers.SDCardDataManger;
import com.vedantu.ext.cmds.db.executors.ExecutorUtils;
import com.vedantu.ext.cmds.db.models.SDCard;
import com.vedantu.ext.cmds.enums.EntityType;
import com.vedantu.ext.cmds.managers.SDCardSyncManager;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;

@WebServlet("/startSDCardDownloads")
public class ImportSDCardResources extends AbstractVedantuServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        super.doPost(req, resp);

        SDCard sdcard = SDCardDataManger.INSTANCE.getSDCard((String) httpParams
                .get(ConstantGlobal.ID));
        LOGGER.debug("Library" + sdcard.getName());
        boolean started = false;
        if (!sdcard.downloaded) {
            ExecutorUtils.executeTask(new SDCardSyncManager((String) httpParams
                    .get(ConstantGlobal.ID), EntityType.SDCARD.name(), org._id));
            started = true;
        }

        JSONObject wRes = new JSONObject();
        wRes.put("sdcard", httpParams.get(ConstantGlobal.ID));
        wRes.put("started", started);
        resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        resp.getWriter().append(wRes.toString());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {

        doPost(req, resp);
    }
}
