package com.vedantu.ext.cmds.managers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import com.vedantu.ext.cmds.db.datamanagers.FileDownloadInfoDataManager;
import com.vedantu.ext.cmds.db.datamanagers.OrgDataManager;
import com.vedantu.ext.cmds.db.models.FileDownloadInfo;
import com.vedantu.ext.cmds.db.models.Organization;
import com.vedantu.ext.cmds.pojo.responses.DownloadableFileInfo;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.utils.config.Config;
import com.vedantu.ext.cmds.utils.web.WebCommunicator;
import com.vedantu.ext.cmds.web.ReqAction;
import com.vedantu.ext.cmds.web.VedantuHttpResponse;

public class FileDownloadManager extends AbstractManager implements Runnable {

    private String entityType = null; // file download info records
    private String id         = null; // file download info records
    private String name       = null; // file download info records
    private String mediaType  = null;
    private String targetId   = null;
    private String targetType = null;

    public FileDownloadManager() {

        super();
    }

    public FileDownloadManager(String type, String id, String name, String mediaType,
            String targetId, String targetType) {

        super();
        this.entityType = type;
        this.id = id;
        this.name = name;
        this.mediaType = mediaType;
        this.targetId = targetId;
        this.targetType = targetType;

    }

    @Override
    public void run() {

        try {
            download();
        } catch (Exception e) {
            LOGGER.error("Error in FileDownloadManager", e);
        }
    }

    public long download() throws Exception {

        LOGGER.debug("File download manager downloading file " + name);
        Organization organization = OrgDataManager.INSTANCE.getOrganization();
        FileDownloadInfo info = FileDownloadInfoDataManager.INSTANCE.getFileDownloadInfo(
                entityType, id, name, targetId, targetType);
        if (info == null) {
            // TODO log file not found
            return -1;
        }
        long totalBytesWrote = 0;

        URL url = null;
        InputStream input = null;
        FileOutputStream output = null;
        try {

            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put(FileDownloadInfo.FIELD_ENTITY_TYPE, info.entityType);
            httpParams.put("fileName", info.name);
            httpParams.put(FileDownloadInfo.FIELD_ENTITY_ID, info.entityId);
            httpParams.put(FileDownloadInfo.FIELD_MEDIA_TYPE, info.mediaType);
            httpParams.put(ConstantGlobal.CALLING_USER_ID, organization.adminUserId);
            httpParams.put(ConstantGlobal.TARGET_USER_ID, organization.adminUserId);
            LOGGER.debug("Fetching data for file Name" + info.name);
            VedantuHttpResponse webRes = WebCommunicator.getResult(ReqAction.GET_SECURE_URL,
                    httpParams);
            checkForErrorResponse(webRes);
            DownloadableFileInfo res = new DownloadableFileInfo();
            webRes.populateResult(res);
            info.size = res.size;
            FileDownloadInfoDataManager.INSTANCE.update(info, FileDownloadInfo.FIELD_SIZE);

            info.downloadStartTime = System.currentTimeMillis();
            String fileDirectory = Config.DESKTOP_LOCATION + File.separator + "desktop_"
                    + entityType.toLowerCase();
            File contentDirectory = new File(fileDirectory);
            if (!contentDirectory.exists()) {
                boolean parentDirectoryCreationResult = contentDirectory.mkdirs();
                if (parentDirectoryCreationResult) {
                    LOGGER.debug("Unable to create content directory"
                            + contentDirectory.getAbsolutePath());
                }

            }

            String decodedURL = URLDecoder.decode(res.downloadUrl, "UTF-8");
            LOGGER.debug("Fetching URL " + URLDecoder.decode(decodedURL, "UTF-8"));

            url = new URL(decodedURL);

            //input = url.openStream();

            String outputFilePath = fileDirectory + File.separator + name;

            synchronized (outputFilePath.intern()) {
                File outputFile = new File(outputFilePath);
                //If the file doesn't exists and size doesn't match get into this loop
                if (!outputFile.exists() || (outputFile.exists()&& outputFile.length() != info.size)) {
                	
                	//Moving the stream opening and logger inside the synchronized block to skip downloading and make downloading faster
                	input = url.openStream();
                	LOGGER.debug("Downloading file" + name + " using url " + url.toString() + " of size "
                            + info.size + " at " + outputFilePath + " " + input.available());
                    
                	output = new FileOutputStream(outputFile);
                    byte[] buffer = new byte[1024*1024*10]; // Adjust if you want
                    int bytesRead;
                    while ((bytesRead = input.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                        totalBytesWrote += bytesRead;
                        LOGGER.debug("File " + outputFilePath + "  downloaded " + totalBytesWrote
                                + " of " + info.size);
                    }
                    output.close();
                } else if (outputFile.length() == info.size) {
                    LOGGER.debug("File already exists");
                    totalBytesWrote = info.size;
                } else {
                    totalBytesWrote = 0;
                }

                if( totalBytesWrote < info.size){
                    LOGGER.debug("Unable to write whole file total bytes wrote " + totalBytesWrote);
                    info.downloaded = false;
                    throw new Exception("Didn't read whole file");
                }
               

                if (totalBytesWrote == info.size) {
                    info.downloadEndTime = System.currentTimeMillis();
                    info.downloaded = true;
                    info.location = fileDirectory;
                }
                    
            }
            LOGGER.debug("Downloaded file" + name + " using url " + url.toString() + " of size "
                    + info.size + " successful");
            
            FileDownloadInfoDataManager.INSTANCE.update(info);

            return totalBytesWrote;
        } catch (Exception e) {
            LOGGER.error("Exception occured", e);
            info.downloaded = false;
            throw e;
        } finally {
            // close url
            FileDownloadInfoDataManager.INSTANCE.update(info, FileDownloadInfo.FIELD_DOWNLOADED);
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    LOGGER.debug("Could not close stream");
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    LOGGER.debug("Could not close stream");
                }
            }
        }

    }

    public String getMediaType() {

        return mediaType;
    }

    public void setMediaType(String mediaType) {

        this.mediaType = mediaType;
    }

}
