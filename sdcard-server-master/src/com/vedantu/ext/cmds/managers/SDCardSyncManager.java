package com.vedantu.ext.cmds.managers;

import com.vedantu.ext.cmds.db.datamanagers.FileDownloadInfoDataManager;
import com.vedantu.ext.cmds.db.datamanagers.OrgDataManager;
import com.vedantu.ext.cmds.db.datamanagers.ResourceDataManager;
import com.vedantu.ext.cmds.db.datamanagers.SDCardDataManger;
import com.vedantu.ext.cmds.db.models.FileDownloadInfo;
import com.vedantu.ext.cmds.db.models.Organization;
import com.vedantu.ext.cmds.db.models.Resource;
import com.vedantu.ext.cmds.db.models.SDCard;
import com.vedantu.ext.cmds.enums.DownloadProcessState;
import com.vedantu.ext.cmds.enums.EntityType;
import com.vedantu.ext.cmds.pojo.responses.DownloadableFileInfo;
import com.vedantu.ext.cmds.pojo.responses.GetFileInfosRes;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.utils.web.WebCommunicator;
import com.vedantu.ext.cmds.web.ReqAction;
import com.vedantu.ext.cmds.web.VedantuHttpResponse;

import javax.servlet.ServletException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SDCardSyncManager extends AbstractManager implements Runnable {

    // public static final LibrarySyncManager INSTANCE = new LibrarySyncManager();
    private String id;

    private String type;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private int    orgKeyId;

    @SuppressWarnings("unused")
    public SDCardSyncManager() {

        super();
    }

    public SDCardSyncManager(String id, String type, int orgKeyId) {

        super();
        this.id = id;
        this.type = type;
        this.orgKeyId = orgKeyId;
    }

    @Override
    public void run() {

        this.sync();
    }

    @SuppressWarnings("WeakerAccess")
    public void sync() {

        LOGGER.debug("Starting sd card sync");
        Organization organization = OrgDataManager.INSTANCE.getOrganization();

        // TODO get org sections for size

        // Get library links
        boolean result = false;
        long downloadedSize = 0;

        SDCard sdcard = SDCardDataManger.INSTANCE.getSDCard(id);
        if (sdcard.state.equals(DownloadProcessState.DOWNLOADED.name())) {
            return;
        }

        sdcard.state = DownloadProcessState.PREPARING.name();
        SDCardDataManger.INSTANCE.update(sdcard);

        try {

            List<Resource> resources = ResourceDataManager.INSTANCE.getResources(id, type, null);
            if (resources.isEmpty()) {
                try {
                    Map<String, Object> paramMap = new HashMap<String, Object>();
                    paramMap.put(ConstantGlobal.TARGET_USER_ID, organization.adminUserId);
                    paramMap.put(ConstantGlobal.CALLING_USER_ID, organization.adminUserId);
                    ResourceManager.INSTANCE.getRemoteResources(organization, id, "SDCARD", paramMap);
                }
                catch (ServletException ignored) {}
                resources = ResourceDataManager.INSTANCE.getResources(id, type, null);
                if (resources.isEmpty()) {
                    LOGGER.debug("No data found for syncing");
                    return;
                }
            }

            sdcard.state = DownloadProcessState.DOWNLOADING.name();
            SDCardDataManger.INSTANCE.update(sdcard);

            sdcard.state = DownloadProcessState.DOWNLOADING.name();
            SDCardDataManger.INSTANCE.update(sdcard);

            for (Resource resource : resources) {
                LOGGER.debug("SDCard Downloading resource " + resource.id + "  " + resource.type);

                Map<String, Object> httpParams = new HashMap<String, Object>();

                httpParams.put("contents[0].id", resource.id);
                httpParams.put("contents[0].type", resource.type);
                httpParams.put(ConstantGlobal.TARGET_USER_ID, organization.adminUserId);
                httpParams.put(ConstantGlobal.CALLING_USER_ID, organization.adminUserId);

                VedantuHttpResponse webRes;
                int retryCount = 0;
                final int MAX_RETRY_COUNT = 5;
                while (true) {
                    webRes = WebCommunicator.getResult(ReqAction.GET_FILE_INFO, httpParams);
                    try {
                        checkForErrorResponse(webRes);
                    } catch (ServletException e) {
                        LOGGER.error("Servlet error", e);
                        if(retryCount++<MAX_RETRY_COUNT){
                            try {
                                Thread.sleep(retryCount*60*1000);
                            } catch (InterruptedException e1) {
                                LOGGER.error("Sleep Interrupted in " + this.getClass().getSimpleName());
                            }
                            continue;
                        }
                    }
                    break;
                }

                GetFileInfosRes res = new GetFileInfosRes();
                webRes.populateResult(res);
                result = true;
                for (DownloadableFileInfo fileInfo : res.list) {

                    FileDownloadInfo info = new FileDownloadInfo(resource.orgKeyId,
                            fileInfo.entityId, fileInfo.entityType, fileInfo.name, sdcard.id,
                            EntityType.SDCARD.name(), fileInfo.downloadUrl, false, fileInfo.size,
                            0, 0, 0, fileInfo.mediaType);
                    try {
                        LOGGER.debug("The entries upserted in FileDownloadInfoDataManager is :"+info);
                        FileDownloadInfoDataManager.INSTANCE.upsert(info);
                        FileDownloadManager manager = new FileDownloadManager(info.entityType,
                                info.entityId, fileInfo.name, fileInfo.mediaType, info.targetId,
                                info.targetType);
                        result &= (manager.download() != -1);

                        if (result) {
                            downloadedSize += fileInfo.size;
                        }

                    } catch (Exception e) {
                        LOGGER.error("Exception occured while downloading", e);
                    }

                }
                sdcard.downloadedSize = downloadedSize;

            }
            LOGGER.debug("Has downloaded all files:" + result);
        } finally {
            if (result) {
                sdcard.downloaded = true;
                sdcard.state = DownloadProcessState.DOWNLOADED.name();
            } else {
                sdcard.state = DownloadProcessState.INITIALIZED.name();
            }
        }

        SDCardDataManger.INSTANCE.update(sdcard);

        LOGGER.debug("Ended library sync");

    }

}
