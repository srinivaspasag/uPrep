package com.vedantu.ext.cmds.servlets;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.vedantu.ext.cmds.db.datamanagers.FileDownloadInfoDataManager;
import com.vedantu.ext.cmds.db.datamanagers.JobInfoDataManager;
import com.vedantu.ext.cmds.db.datamanagers.ResourceDataManager;
import com.vedantu.ext.cmds.db.datamanagers.SDCardDataManger;
import com.vedantu.ext.cmds.db.datamanagers.SDCardGroupDataManager;
import com.vedantu.ext.cmds.db.executors.ExecutorUtils;
import com.vedantu.ext.cmds.db.models.Job;
import com.vedantu.ext.cmds.db.models.SDCard;
import com.vedantu.ext.cmds.db.models.SDCardGroup;
import com.vedantu.ext.cmds.enums.EntityType;
import com.vedantu.ext.cmds.managers.PackageManager;
import com.vedantu.ext.cmds.pojo.responses.local.GetPackResponse;
import com.vedantu.ext.cmds.utils.commons.StringUtils;
import com.vedantu.ext.cmds.utils.config.Config;

@WebServlet("/pack")
public class FlashSDCard extends AbstractVedantuServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {

        super.doPost(req, resp);

        String sdCardId = (String) httpParams.get("sdCardId");
        String groupDirectory = (String) httpParams.get("groupDirectory");
        SDCard card = SDCardDataManger.INSTANCE.getSDCard(sdCardId);
        String location = (String) httpParams.get("location");
        if (card == null) {
            throw new ServletException("no card found");
        }
        LOGGER.debug("Group Directory Specified" + groupDirectory);

        int resourceCount = ResourceDataManager.INSTANCE.getResourceCount(sdCardId,
                EntityType.SDCARD.name());
        int fileCount = FileDownloadInfoDataManager.INSTANCE.getFileDownloadInfoCount(sdCardId,
                EntityType.SDCARD.name());
        Job job = new Job(org._id, resourceCount + fileCount, sdCardId, EntityType.SDCARD.name());

        try {
            job._id = JobInfoDataManager.INSTANCE.insert(job);
        } catch (Exception e) {
            throw new ServletException("can not create jobs for this one");
        }
        ExecutorUtils.executeTask(new PackageManager(card, location, job._id));

        SDCardGroup group = SDCardGroupDataManager.INSTANCE.getSDCardGroup(card.groupId);
        if (group == null) {
            throw new ServletException("no card group found");
        }
//        String location = Config.DESKTOP_LOCATION + File.separator + "flashed" + File.separator
//                + StringUtils.strip(group.name);
//
//        String desktopGroupLocation = Config.DESKTOP_LOCATION + File.separator + "flashed"
//                + File.separator + StringUtils.strip(group.name);
        File sdCardDirectory = new File(location + File.separator
                + StringUtils.strip((StringUtils.isNotEmpty(card.name) ? card.name : card.id))
                + File.separator + "learnpedia");
        LOGGER.debug("SD Card directory creating if necessary " + sdCardDirectory.getAbsolutePath());
        if (!sdCardDirectory.exists()) {
            sdCardDirectory.mkdirs();
        }

        GetPackResponse response = new GetPackResponse(job._id, sdCardDirectory.getAbsolutePath(), card.id);

        JSONObject res = new JSONObject(response);
        printJSONResponse(resp, res);

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        this.doGet(req, resp);
    }
}
