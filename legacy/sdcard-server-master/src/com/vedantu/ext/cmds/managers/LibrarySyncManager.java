package com.vedantu.ext.cmds.managers;

import com.vedantu.ext.cmds.db.datamanagers.FileDownloadInfoDataManager;
import com.vedantu.ext.cmds.db.datamanagers.ImportedLibraryDataManager;
import com.vedantu.ext.cmds.db.datamanagers.OrgDataManager;
import com.vedantu.ext.cmds.db.datamanagers.ResourceDataManager;
import com.vedantu.ext.cmds.db.models.FileDownloadInfo;
import com.vedantu.ext.cmds.db.models.ImportedLibrary;
import com.vedantu.ext.cmds.db.models.Organization;
import com.vedantu.ext.cmds.db.models.Resource;
import com.vedantu.ext.cmds.enums.DownloadProcessState;
import com.vedantu.ext.cmds.enums.LibraryState;
import com.vedantu.ext.cmds.pojo.responses.*;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.utils.commons.StringUtils;
import com.vedantu.ext.cmds.utils.config.Config;
import com.vedantu.ext.cmds.utils.web.WebCommunicator;
import com.vedantu.ext.cmds.web.JSONUtils;
import com.vedantu.ext.cmds.web.ReqAction;
import com.vedantu.ext.cmds.web.VedantuHttpResponse;
import org.apache.http.util.TextUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.ServletException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class LibrarySyncManager extends AbstractManager implements Runnable {

    // public static final LibrarySyncManager INSTANCE = new LibrarySyncManager();
    private String id;

    private String type;
    private int    orgKeyId;
    private static final String  baseUrl = "http://localhost:8080";
    private static final int RESPONSE_SIZE = 50;
    private static final String START = "start";
    private static final String SIZE = "size";
    private static final String IMG_SRC = "src";
    private static final String IMG_IDENTIFIER = "v-uid";

    public LibrarySyncManager() {

        super();
    }

    public LibrarySyncManager(String id, String type, int orgKeyId) {

        super();
        this.id = id;
        this.type = type;
        this.orgKeyId = orgKeyId;
    }

    public ImportedLibrary insertImportLibraryEvent(int orgKeyId, String id, String type,
                                                    String name, long size) {

        ImportedLibrary syncedLibrary = new ImportedLibrary(orgKeyId, id, type, name, size);
        syncedLibrary.state = LibraryState.INITIALIZED.name();
        try {
            syncedLibrary = ImportedLibraryDataManager.INSTANCE.upsert(syncedLibrary);

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return syncedLibrary;
    }

    @Override
    public void run() {

        download();
    }

    public void download() {

        Organization organization = OrgDataManager.INSTANCE.getOrganization();

        // Get library links
        boolean result = true;

        ImportedLibrary library = ImportedLibraryDataManager.INSTANCE.getSyncedLibrary(orgKeyId, id);
        LOGGER.debug("Starting library sync" + library.id);
        try {

            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("sectionId", library.id);
            VedantuHttpResponse webRes = WebCommunicator.getResult(ReqAction.GET_LIBRARY_INFO, httpParams);
            LOGGER.debug("Starting library response" + webRes);
            checkForErrorResponse(webRes);
            GetOrgSectionInfoRes res = new GetOrgSectionInfoRes();
            webRes.populateResult(res);


            //Fetching content Links and saving to File
            Map<String, Object> linkParams = new HashMap<String, Object>();
            int start = 0;
            linkParams.put(ConstantGlobal.TARGET_USER_ID, organization.adminUserId);
            linkParams.put(ConstantGlobal.CALLING_USER_ID, organization.adminUserId);
            linkParams.put(ConstantGlobal.USER_ID, organization.adminUserId);
            linkParams.put(ConstantGlobal.ORG_ID, organization.id);
            linkParams.put("target.id", library.id);
            linkParams.put("target.type", "SECTION");
            linkParams.put("linkType", "ADDED");
            linkParams.put(SIZE, RESPONSE_SIZE);
            linkParams.put(START, start);
            File contentLinkDir = new File(Config.DESKTOP_LOCATION + File.separator + "LINKS");
            if(!contentLinkDir.exists()) {
                boolean mkdirs = contentLinkDir.mkdirs();
                if(!mkdirs) {
                    LOGGER.error("Unable to create folders " + contentLinkDir.getAbsolutePath());
                    return;
                }
            }

            //This request is only for fetching total hits.
            VedantuHttpResponse contentLinkResponse = WebCommunicator.getResult(ReqAction.GET_CONTENT_LINKS, linkParams);
            GetResourcesRes contentLinksRes = new GetResourcesRes();
            contentLinkResponse.populateResult(contentLinksRes);
            int totalHits = contentLinksRes.totalHits;

            linkParams.put("addContent", "true");

            File contentLinksFile = new File(contentLinkDir, "Content_Links");
            BufferedWriter contentLinkWriter = new BufferedWriter(new FileWriter(contentLinksFile));
            File testDirectory = new File(Config.DESKTOP_LOCATION + File.separator +"test");
            if(! testDirectory.exists()){
                boolean mkdirs = testDirectory.mkdirs();
                if(!mkdirs) {
                    LOGGER.error("Unable to create folders at " + testDirectory.getAbsolutePath());
                    return;
                }
            }
            BufferedWriter testIdWriter = new BufferedWriter(new FileWriter(new File(testDirectory, "Test_ids")));
            Set<String> moduleIds = new HashSet<String>();
            while(start < totalHits) {
                linkParams.put(START, start);
                contentLinkResponse = WebCommunicator.getResult(ReqAction.GET_CONTENT_LINKS, linkParams);
                contentLinksRes = new GetResourcesRes();
                contentLinkResponse.populateResult(contentLinksRes);
                String contentLinkString = contentLinkResponse.toJSON().toString();
                LOGGER.debug("Link: " + contentLinkString);
                contentLinkWriter.write(contentLinkString);
                contentLinkWriter.newLine();
                for(GetResourceRes contentLinkRes:contentLinksRes.list) {
                    if(contentLinkRes.type.equals("MODULE")) {
                        moduleIds.add(contentLinkRes.id);
                    }
                    else if(contentLinkRes.type.equals("TEST")) {
                        String testId = contentLinkRes.id;
                        testIdWriter.write(testId);
                        testIdWriter.newLine();
                        Map<String, Object> testQuestionParams = new HashMap<String, Object>();
                        testQuestionParams.put(ConstantGlobal.TARGET_USER_ID, organization.adminUserId);
                        testQuestionParams.put(ConstantGlobal.CALLING_USER_ID, organization.adminUserId);
                        testQuestionParams.put("addAnswer", true);
                        testQuestionParams.put(ConstantGlobal.TYPE,"QUESTION");
                        int i = 0;
                        for(String qId:contentLinkRes.extraInfo) {
                            testQuestionParams.put("ids" + "[" + i + "]", qId);
                            i++;
                        }
                        VedantuHttpResponse testQResp = WebCommunicator.getResult(ReqAction.GET_CONTENTS, testQuestionParams);
                        BufferedWriter singleTestWriter = new BufferedWriter(new FileWriter(new File(testDirectory, testId)));
                        JSONObject singleTestAnswer = testQResp.toJSON();
                        JSONObject singleTestResult = singleTestAnswer.getJSONObject("result");
                        JSONArray jArray = JSONUtils.getJSONArray(singleTestResult, "list");
                        int total = jArray.length();
                        for (int j = 0; j < total; j++) {
                            JSONObject contentJSON = jArray.getJSONObject(j);
                            updateQuestion(contentJSON);
                            jArray.put(j, contentJSON);
                        }
                        singleTestResult.put("list", jArray);
                        singleTestAnswer.put("result", singleTestResult);
                        String value = singleTestAnswer.toString();
                        singleTestWriter.write(value);
                        singleTestWriter.newLine();
                        singleTestWriter.flush();
                        singleTestWriter.close();
                    }
                }
                start += RESPONSE_SIZE;
            }
            testIdWriter.flush();

            contentLinkWriter.flush();
            contentLinkWriter.close();

            BufferedWriter moduleLinkWriter;


            if(! testDirectory.exists()){
                boolean mkdirs = testDirectory.mkdirs();
                if(!mkdirs) {
                    LOGGER.error("Unable to create folders at " + testDirectory.getAbsolutePath());
                    return;
                }
            }

            File moduleLinksFile = new File(contentLinkDir, "Module_Links");
            if (!moduleIds.isEmpty()) {
                moduleLinkWriter = new BufferedWriter(new FileWriter(moduleLinksFile));

                linkParams.put("target.type", "MODULE");
                linkParams.put(START, 0);
                linkParams.remove(SIZE);

                for(String moduleId:moduleIds) {
                    linkParams.put("target.id", moduleId);
                    VedantuHttpResponse moduleResponse = WebCommunicator.getResult(ReqAction.GET_CONTENT_LINKS, linkParams);
                    moduleLinkWriter.write(moduleResponse.toJSON().toString());
                    moduleLinkWriter.newLine();
                    moduleLinkWriter.flush();
                    //Fetching Test Ids within the module
                    GetResourcesRes moduleContentLinks = new GetResourcesRes();
                    moduleResponse.populateResult(moduleContentLinks);

                    for(GetResourceRes moduleContent:moduleContentLinks.list){
                        Resource resource = new Resource(organization._id, organization.adminUserId, moduleContent.id, moduleContent.type,
                                moduleContent.name,moduleContent.targetId, moduleContent.targetType, moduleContent.timeCreated, moduleContent.subType,
                                moduleContent.thumbnail, moduleContent.size, moduleContent.extraInfo);
                        try {
                            resource = ResourceDataManager.INSTANCE.upsert(resource);
                            LOGGER.debug("Inserting reosource to db "+ resource.id + "  "+ resource.type);
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }

                        if(moduleContent.type.equals("TEST")) {
                            Map<String, Object> testQuestionParams = new HashMap<String, Object>();
                            testQuestionParams.put(ConstantGlobal.TARGET_USER_ID, organization.adminUserId);
                            testQuestionParams.put(ConstantGlobal.CALLING_USER_ID, organization.adminUserId);
                            testQuestionParams.put("addAnswer", true);
                            testQuestionParams.put(ConstantGlobal.TYPE,"QUESTION");
                            int i = 0;
                            for(String qId:moduleContent.extraInfo) {
                                testQuestionParams.put("ids" + "[" + i + "]", qId);
                                i++;
                            }
                            VedantuHttpResponse moduleQResp = WebCommunicator.getResult(ReqAction.GET_CONTENTS, testQuestionParams);
                            try {
                                checkForErrorResponse(moduleQResp);
                            } catch (ServletException e) {
                                LOGGER.error(e.getMessage(), e);
                            }
                            JSONObject singleTestAnswer = moduleQResp.toJSON();
                            JSONObject singleTestResult = singleTestAnswer.getJSONObject("result");
                            JSONArray jArray = JSONUtils.getJSONArray(singleTestResult, "list");
                            int total = jArray.length();
                            for (int j = 0; j < total; j++) {
                                JSONObject contentJSON = jArray.getJSONObject(j);
                                updateQuestion(contentJSON);
                                jArray.put(j, contentJSON);
                            }
                            String testId = moduleContent.id;
                            File singleTestFile = new File(testDirectory, testId);
                            BufferedWriter bufferedTestWriter = new BufferedWriter(new FileWriter(singleTestFile));
                            singleTestResult.put("list", jArray);
                            singleTestAnswer.put("result", singleTestResult);
                            String value = singleTestAnswer.toString();
                            bufferedTestWriter.write(value);
                            bufferedTestWriter.newLine();
                            bufferedTestWriter.flush();
                            bufferedTestWriter.close();
                            testIdWriter.write(testId);
                            testIdWriter.newLine();
                        }
                    }
                }

                moduleLinkWriter.flush();
                moduleLinkWriter.close();
            }
            testIdWriter.flush();
            testIdWriter.close();

            if(!contentLinksFile.exists()) {
                LOGGER.error("Content Links file hasn't been generated");
                throw new RuntimeException("Stop everything, content links file error");
            }

            library.size = res.info.size;
            library.state = LibraryState.PREPARING.name();

            ImportedLibraryDataManager.INSTANCE.update(library);

            List<Resource> resources = ResourceDataManager.INSTANCE.getResources(id, type, null);

            if (resources.isEmpty()) {
                try {

                    Map<String, Object> paramMap = new HashMap<String, Object>();
                    paramMap.put(ConstantGlobal.TARGET_USER_ID, organization.adminUserId);
                    paramMap.put(ConstantGlobal.CALLING_USER_ID, organization.adminUserId);

                    ResourceManager.INSTANCE.getRemoteResources(organization, id, "SECTION",
                            paramMap);
                } catch (ServletException e) {
                    library.state = LibraryState.INITIALIZED.name();
                }
                resources = ResourceDataManager.INSTANCE.getResources(id, type, null);
                if (resources.isEmpty()) {
                    LOGGER.debug("No data found for syncing");
                    return;
                }
            }
            library.state = DownloadProcessState.DOWNLOADING.name();
            library.downloadedSize = 0;
            ImportedLibraryDataManager.INSTANCE.update(library);

            for (Resource resource : resources) {
                library = ImportedLibraryDataManager.INSTANCE.getSyncedLibrary(organization._id,
                        library.id);
                if (library.state.equals(DownloadProcessState.CANCELLED.name())) {

                    throw new Exception("User cancelled library sync");
                }
                LOGGER.debug("Downloading resource " + resource.id + "  " + resource.type);

                Map<String, Object> fileInfoParams = new HashMap<String, Object>();

                fileInfoParams.put("contents[0].id", resource.id);
                fileInfoParams.put("contents[0].type", resource.type);
                fileInfoParams.put(ConstantGlobal.TARGET_USER_ID, organization.adminUserId);
                fileInfoParams.put(ConstantGlobal.CALLING_USER_ID, organization.adminUserId);

                int retryCount = 0;
                final int MAX_RETRY_COUNT = 5;
                while(true){
                    webRes = WebCommunicator.getResult(ReqAction.GET_FILE_INFO, fileInfoParams);
                    try {
                        checkForErrorResponse(webRes);
                    } catch (ServletException e) {
                        if(retryCount++<MAX_RETRY_COUNT){
                            Thread.sleep(retryCount*60*1000);
                            continue;
                        }
                        library.state = LibraryState.ABORTED.name();
                        result = false;
                        return;
                    }
                    break;
                }

                GetFileInfosRes fileInfoResponse = new GetFileInfosRes();
                webRes.populateResult(fileInfoResponse);

                for (DownloadableFileInfo fileInfo : fileInfoResponse.list) {

                    FileDownloadInfo info = new FileDownloadInfo(resource.orgKeyId,
                            fileInfo.entityId, fileInfo.entityType, fileInfo.name, library.id,
                            library.type, fileInfo.downloadUrl, false, fileInfo.size, 0, 0, 0,
                            fileInfo.mediaType);
                    try {

                        FileDownloadInfoDataManager.INSTANCE.insertIfDoesntExist(info);
                        FileDownloadManager manager = new FileDownloadManager(info.entityType,
                                info.entityId, fileInfo.name, fileInfo.mediaType, info.targetId,
                                info.targetType);
                        retryCount = 0;
                        while(retryCount++ < MAX_RETRY_COUNT){
                            try{
                                library.downloadedSize += manager.download();
                            }catch(Exception networkException){
                                Thread.sleep(retryCount*60*1000); // 1min,2min,3min...
                                continue;
                            }
                            break;
                        }

                    } catch (Exception e) {
                        result = false;
                        LOGGER.error("Exception occured while downloading", e);
                        LOGGER.error("Retrying download, current count is " + retryCount);
                        library.state = DownloadProcessState.ABORTED.name();
                        throw e;

                    }
                    ImportedLibraryDataManager.INSTANCE.update(library);

                }

            }
            LOGGER.debug("Has downloaded all files:" + true);
        } catch (Exception exception) {

            LOGGER.error(exception.getMessage(), exception);
            library.state = DownloadProcessState.ABORTED.name();

        } finally {
            if (result) {
                library.downloaded = true;
                library.state = LibraryState.DOWNLOADED.name();
            }
            ImportedLibraryDataManager.INSTANCE.update(library);
        }

        LOGGER.debug("Ended library sync");

    }

    private void updateQuestion(JSONObject content) {
        String question = JSONUtils.getString(content, ConstantGlobal.DESC);
        question = removeImageSrcUrl(question, "question");
        JSONUtils.putValue(ConstantGlobal.DESC, question, content);
        JSONObject infoJSON = new JSONObject(JSONUtils.getString(content, "info"));
        JSONObject matrix = JSONUtils.getJSONObject(infoJSON, "matrix");
        JSONArray options = JSONUtils.getJSONArray(infoJSON, "options");

        for(int i=0; i<options.length(); i++) {
            String optionString = options.getString(i);
            optionString = removeImageSrcUrl(optionString, "question");
            options.put(i, optionString);
        }
        if (matrix != null) {
            @SuppressWarnings("unchecked")
            Iterator<String> it = matrix.keys();
            String key;
            while (it.hasNext()) {
                key = it.next();
                JSONArray jArray = updateQuestionArray(matrix.getJSONArray(key));
                matrix.put(key, jArray);
            }
        }
        JSONObject solJSON = JSONUtils.getJSONObject(infoJSON, "solution");
        solJSON.put(ConstantGlobal.CONTENT, removeImageSrcUrl(JSONUtils.getString(solJSON, ConstantGlobal.CONTENT)
                , "question"));
        JSONUtils.putValue("matrix", matrix, infoJSON);
        JSONUtils.putValue("options", options, infoJSON);
        JSONUtils.putValue("solution", solJSON, infoJSON);
        JSONUtils.putValue("info", infoJSON.toString(), content);

    }
    private JSONArray updateQuestionArray(JSONArray jsonArray) throws JSONException {

        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonArray.put(i, removeImageSrcUrl(jsonArray.getString(i), "question"));
            }
        }
        return jsonArray;
    }

    private String removeImageSrcUrl(String html, String entityType) {

        if (TextUtils.isEmpty(html)) {
            LOGGER.error("Question content HTML is empty");

            return "";
        }

        Document doc = Jsoup.parseBodyFragment(html);
        Elements elements = doc.getElementsByAttribute(IMG_IDENTIFIER);

        for (Element es : elements) {
            String uuid = es.attr(IMG_IDENTIFIER);// v-uid=uuid
            String url = es.attr(IMG_SRC);
            LOGGER.debug("uuid " + uuid + ", imgSrc: " + url);
            es.attr(IMG_SRC, StringUtils.join(File.separator,
                    baseUrl, "sdcard", "vedantu",
                            entityType,
                            StringUtils.substringAfterLast(url, File.separator)));
        }

        return doc.body().html();
    }

}
