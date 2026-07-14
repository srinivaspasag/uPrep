/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import controllers.AbstractUIController.JSONResponseWrapper;
import play.Logger;
import play.cache.Cache;
import play.data.validation.Required;
import play.libs.F.Promise;
import play.mvc.Scope;
import play.mvc.With;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;
import uicom.util.Validation;

/**
 *
 * @author anirbandutta
 */
@With(Security.class)
public class MyContents extends AbstractUIController {

    protected static JSONObject fetchDocumentsService(Map<String, Object> allParams) {
        return null;
        /*if(Boolean.parseBoolean(Play.configuration.getProperty("DOC_VIEWER_FEATURE"))){
        allParams.put("excludeTypes[0]", "video");
        allParams.remove("includeTypes[0]");
        Promise<JSONResponseWrapper> promise = null;
        String isExploreContent = Scope.Params.current().get("exploreContentPage");
        if(isExploreContent!=null && isExploreContent.length()>0){
        promise = client(ClientUtil.RECOS_WEB_SERVICE_URL +"/recommendations/getDocuments", allParams);
        }else{
        promise = client(ClientUtil.LIB_WEB_SERVICE_URL +"/documents/getDocuments", allParams);
        }
        Logger.log4j.info("BEFORE AWAIT : GET MY CONTENTS - DOCUMENTS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : GET MY CONTENTS - DOCUMENTS");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        return data;
        }else {
        return null;
        }*/
    }

    protected static JSONObject _getVideos(Map<String, Object> allParams) {
        Logger.log4j.info("Key :: "+Scope.Params.current().get("orgId") + Scope.Params.current().get("programId")
                        + Scope.Params.current().get("centerId")
                        + Scope.Params.current().get("sectionId")
                        + Scope.Params.current().get("start")
                        + request.params.get("brdIds[0]") + request.params.get("orderBy")
                        +"_getVideos");
        String resp = Cache.get(
                Scope.Params.current().get("orgId") + Scope.Params.current().get("programId")
                        + Scope.Params.current().get("centerId")
                        + Scope.Params.current().get("sectionId")
                        + Scope.Params.current().get("start")
                        + request.params.get("brdIds[0]") + request.params.get("orderBy")
                        +"_getVideos", String.class);
        if(!StringUtils.isEmpty(resp)){
            Logger.log4j.info("Served _getVideos from cache");
            JSONObject data = getCacheDataInJsonObject(resp);
            return data;
        }else{
            Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/videos/getVideos", allParams);
    //        String isExploreContent = Scope.Params.current().get("exploreContentPage");
            Logger.log4j.info("BEFORE AWAIT : GET MY CONTENTS - VIDEOS");
            await(promise);
            Logger.log4j.info("AFTER AWAIT : GET MY CONTENTS - VIDEOS");
            JSONObject data = getJSON(promise);
            data = Validation.verifyResponse(data);
            if(data != null)
                Cache.set(Scope.Params.current().get("orgId") + Scope.Params.current().get("programId")
                        + Scope.Params.current().get("centerId")
                        + Scope.Params.current().get("sectionId")
                        + Scope.Params.current().get("start")
                        + request.params.get("brdIds[0]") + request.params.get("orderBy")
                        +"_getVideos", data.toString(), "5mn");
            return data;
        }
    }

    protected static JSONObject _getDocuments(Map<String, Object> allParams) {
        Logger.log4j.info("Key :: "+Scope.Params.current().get("orgId") + Scope.Params.current().get("programId")
                        + Scope.Params.current().get("centerId")
                        + Scope.Params.current().get("sectionId")
                        + Scope.Params.current().get("start")
                        + request.params.get("brdIds[0]") + request.params.get("orderBy")
                        +"_getDocuments");
        String resp = Cache.get(
                Scope.Params.current().get("orgId") + Scope.Params.current().get("programId")
                        + Scope.Params.current().get("centerId")
                        + Scope.Params.current().get("sectionId")
                        + Scope.Params.current().get("start")
                        + request.params.get("brdIds[0]") + request.params.get("orderBy")
                        +"_getDocuments", String.class);
        if(!StringUtils.isEmpty(resp)){
            Logger.log4j.info("Served _getDocuments from cache");
            JSONObject data = getCacheDataInJsonObject(resp);
            return data;
        }else{
            Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/documents/getDocuments", allParams);
    //        String isExploreContent = Scope.Params.current().get("exploreContentPage");
            Logger.log4j.info("BEFORE AWAIT : GET MY CONTENTS - DOCUMENTS");
            await(promise);
            Logger.log4j.info("AFTER AWAIT : GET MY CONTENTS - DOCUMENTS");
            JSONObject data = getJSON(promise);
            data = Validation.verifyResponse(data);
            if(data != null)
                Cache.set(Scope.Params.current().get("orgId") + Scope.Params.current().get("programId")
                        + Scope.Params.current().get("centerId")
                        + Scope.Params.current().get("sectionId")
                        + Scope.Params.current().get("start")
                        + request.params.get("brdIds[0]") + request.params.get("orderBy")
                        +"_getDocuments", data.toString(), "5mn");
            return data;
        }
    }

    protected static JSONObject _getFiles(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/files/getFiles", allParams);
//        String isExploreContent = Scope.Params.current().get("exploreContentPage");
        Logger.log4j.info("BEFORE AWAIT : GET MY CONTENTS - FILES");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : GET MY CONTENTS - FILES");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        return data;
    }

    protected static JSONObject _getModules(Map<String, Object> allParams) {
        Logger.log4j.info("Key :: "+Scope.Params.current().get("orgId") + Scope.Params.current().get("programId")
                        + Scope.Params.current().get("centerId")
                        + Scope.Params.current().get("sectionId")
                        + request.params.get("brdIds[0]") + request.params.get("orderBy")
                        +"_getModules");
        String resp = Cache.get(
                Scope.Params.current().get("orgId") + Scope.Params.current().get("programId")
                        + Scope.Params.current().get("centerId")
                        + Scope.Params.current().get("sectionId")
                        + Scope.Params.current().get("start")
                        + request.params.get("brdIds[0]") + request.params.get("orderBy")
                        +"_getModules", String.class);
        if(!StringUtils.isEmpty(resp)){
            Logger.log4j.info("Served _getModules from cache");
            JSONObject data = getCacheDataInJsonObject(resp);
            return data;
        }else{
            Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                    + "/modules/getModules", allParams);
            // String isExploreContent =
            // Scope.Params.current().get("exploreContentPage");
            Logger.log4j.info("BEFORE AWAIT : GET MY CONTENTS - MODULES");
            await(promise);
            Logger.log4j.info("AFTER AWAIT : GET MY CONTENTS - MODULES");
            JSONObject data = ResponseUtil.checkResponse(getJSON(promise));
            if(data != null)
                Cache.set(Scope.Params.current().get("orgId") + Scope.Params.current().get("programId")
                        + Scope.Params.current().get("centerId")
                        + Scope.Params.current().get("sectionId")
                        + Scope.Params.current().get("start")
                        + request.params.get("brdIds[0]") + request.params.get("orderBy")
                        +"_getModules", data.toString(), "5mn");
            return data;
        }
    }

    protected static JSONObject fetchTestService(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = null;
        String isExploreContent = Scope.Params.current().get("exploreContentPage");
        if (isExploreContent != null && isExploreContent.length() > 0) {
            promise = client(ClientUtil.RECOS_WEB_SERVICE_URL + "/recommendations/getTests", allParams);
        } else {
            promise = client(ClientUtil.QUESTIONS_WEB_SERVICE_URL + "/Tests/getTests", allParams);
        }
        Logger.log4j.info("BEFORE AWAIT : GET MY CONTENTS - TESTS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : GET MY CONTENTS - TESTS");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        return data;
    }

    protected static JSONObject fetchPlaylistService(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = null;
        JSONObject data = null;
        String isExploreContent = Scope.Params.current().get("exploreContentPage");
        if (isExploreContent != null && isExploreContent.length() > 0) {
            promise = client(ClientUtil.RECOS_WEB_SERVICE_URL + "/recommendations/getPlaylists", allParams);
            Logger.log4j.info("BEFORE AWAIT : GET MY CONTENTS - PLAYLISTS");
            await(promise);
            Logger.log4j.info("AFTER AWAIT : GET MY CONTENTS - PLAYLISTS");
            data = getJSON(promise);
        } else {
            data = Playlists._getPlaylists(allParams);
        }
        data = Validation.verifyResponse(data);
        return data;
    }

    protected static JSONObject fetchRecommendationService(String url, Map<String, Object> allParams) {
        return null;
        /*Promise<JSONResponseWrapper> promise = client(url, allParams);
        Logger.log4j.info("BEFORE AWAIT : GET MY CONTENTS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : GET MY CONTENTS");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        return data;*/
    }

    public static void index() {
        Map<String, Object> allParams = getReqParams();
        Boolean showMyContents = true;
        String targetUserId = Scope.Params.current().get("targetUserId");
        String isExploreContent = Scope.Params.current().get("exploreContentPage");
        String openTabId = Scope.Params.current().get("tabId");
        if (openTabId != null && openTabId.length() > 0) {
            openTabId = openTabId.toUpperCase();//DECIDE Entry Tab
        } else {
            openTabId = "";
        }
        if (isExploreContent != null && isExploreContent.length() > 0) {
            session.put("isExploreContentPage", "true");
            allParams.put("resultType", "ALL");
            showMyContents = false;
        } else {
            session.put("isExploreContentPage", "false");
            allParams.put("resultType", "FOLLOWING");
        }
        allParams.put("start", 0);
        allParams.put("sortOrder", "DESC");
        allParams.put("orderBy", "views");
        if (openTabId.equals("") || openTabId == null) {
            allParams.put("size", 3);
        } else {
            allParams.put("size", 12);
        }
        JSONObject documents = null;
        JSONObject playlist = null;
        JSONObject tests = null;
        JSONObject videos = null;
        if ("DOCUMENTS".equals(openTabId)) {
            documents = fetchDocumentsService(allParams);
        } else if ("PLAYLISTS".equals(openTabId)) {
            playlist = fetchPlaylistService(allParams);
        } else if ("TESTS".equals(openTabId)) {
            tests = fetchTestService(allParams);
        } else if ("VIDEOS".equals(openTabId)) {
            videos = _getVideos(allParams);
        } else {
            tests = fetchTestService(allParams);
            playlist = fetchPlaylistService(allParams);
        }
        //JSONObject recommendations = fetchRecommendationService(ClientUtil.RECOS_WEB_SERVICE_URL +"recommendations/getRecommendations", allParams);
        render(showMyContents, playlist, documents, tests, videos, targetUserId, isExploreContent, openTabId);
    }

    protected static String getViewPath(String fileName) {
        String viewType = Scope.Params.current().get("viewType");
        String path = "iconView";
        String pre = "tags/myContents/";
        if ("list".equals(viewType)) {
            path = "listView";
        }
        String file = pre.concat(path).concat("/" + fileName);
        return file;
    }

    public static void getRecommendations() {
        Map<String, Object> allParams = getReqParams();
        JSONObject recommendations = fetchRecommendationService(ClientUtil.RECOS_WEB_SERVICE_URL + "recommendations/getRecommendations", allParams);
        String file = getViewPath("getRecommendations.html");
        render("tags/myContents/items.html", file, recommendations);
    }

    public static void getPlaylists() {
        Map<String, Object> allParams = getReqParams();
        JSONObject playlist = fetchPlaylistService(allParams);
        String file = getViewPath("getPlaylists.html");
        render("tags/myContents/items.html", file, playlist);
    }

    public static void getDocuments() {
        Map<String, Object> allParams = getReqParams();
        JSONObject documents = fetchDocumentsService(allParams);
        String file = getViewPath("getDocuments.html");
        render("tags/myContents/items.html", file, documents);
    }

    public static void getTests() {
        Map<String, Object> allParams = getReqParams();
        JSONObject tests = fetchTestService(allParams);
        String file = getViewPath("getTests.html");
        render("tags/myContents/items.html", file, tests);
    }

    public static void getVideos() {
        Map<String, Object> allParams = getReqParams();
        JSONObject videos = _getVideos(allParams);
        String file = getViewPath("getVideos.html");
        render("tags/myContents/items.html", file, videos);
    }

    public static void exploreContentOf() {
        Map<String, Object> allParams = getReqParams();
        Boolean showMyContents = false;
        String isExploreContent = "true";
        String openTabId = Scope.Params.current().get("tabId");
        if (openTabId != null && openTabId.length() > 0) {
            openTabId = openTabId.toUpperCase();//DECIDE Entry Tab
        } else {
            openTabId = "";
        }
        session.put("isExploreContentPage", "true");
        allParams.put("start", 0);
        allParams.put("sortOrder", "DESC");
        allParams.put("orderBy", "views");
        allParams.put("resultType", "ALL");
        if (openTabId.equals("") || openTabId == null) {
            allParams.put("size", 3);
        } else {
            allParams.put("size", 12);
        }
        allParams.put("exploreContentPage", "true");
        Scope.Params.current().put("exploreContentPage", "true");
        JSONObject documents = null;
        JSONObject playlist = null;
        JSONObject tests = null;
        JSONObject videos = null;
        if ("DOCUMENTS".equals(openTabId)) {
            documents = fetchDocumentsService(allParams);
        } else if ("PLAYLISTS".equals(openTabId)) {
            playlist = fetchPlaylistService(allParams);
        } else if ("TESTS".equals(openTabId)) {
            tests = fetchTestService(allParams);
        } else if ("VIDEOS".equals(openTabId)) {
            videos = _getVideos(allParams);
        } else {
            tests = fetchTestService(allParams);
            playlist = fetchPlaylistService(allParams);
        }
        render("MyContents/index.html", showMyContents, playlist, documents, tests, videos, isExploreContent, openTabId);
    }

    //url mapping
    public static void myContentDirect(String tabId) {
        Map<String, Object> allParams = getReqParams();
        Boolean showMyContents = true;
        String isExploreContent = "";
        session.put("isExploreContentPage", "false");
        String openTabId = tabId != null && tabId.length() > 0 ? tabId : Scope.Params.current().get("tabId");
        if (openTabId != null && openTabId.length() > 0) {
            openTabId = openTabId.toUpperCase();//DECIDE Entry Tab
        } else {
            openTabId = "";
        }
        allParams.put("start", 0);
        allParams.put("sortOrder", "DESC");
        allParams.put("orderBy", "views");
        allParams.put("resultType", "FOLLOWING");
        if (openTabId.equals("") || openTabId == null) {
            allParams.put("size", 3);
        } else {
            allParams.put("size", 12);
        }
        JSONObject documents = null;
        JSONObject playlist = null;
        JSONObject tests = null;
        JSONObject videos = null;
        if ("DOCUMENTS".equals(openTabId)) {
            documents = fetchDocumentsService(allParams);
        } else if ("PLAYLISTS".equals(openTabId)) {
            playlist = fetchPlaylistService(allParams);
        } else if ("TESTS".equals(openTabId)) {
            tests = fetchTestService(allParams);
        } else if ("VIDEOS".equals(openTabId)) {
            videos = _getVideos(allParams);
        } else {
            tests = fetchTestService(allParams);
            playlist = fetchPlaylistService(allParams);
        }
        String includeName = "MyContents/index.html";
        flash.put("ENTRY", "DIRECT");
        render("Application/myPages.html", includeName, showMyContents, playlist, documents, tests, videos, isExploreContent, openTabId);
    }

    public static void exploreContentDirect(String tabId) {
        Map<String, Object> allParams = getReqParams();
        Boolean showMyContents = false;
        String isExploreContent = "true";
        session.put("isExploreContentPage", "true");
        String openTabId = tabId != null && tabId.length() > 0 ? tabId : Scope.Params.current().get("tabId");
        if (openTabId != null && openTabId.length() > 0) {
            openTabId = openTabId.toUpperCase();//DECIDE Entry Tab
        } else {
            openTabId = "";
        }
        allParams.put("start", 0);
        allParams.put("sortOrder", "DESC");
        allParams.put("orderBy", "views");
        allParams.put("resultType", "ALL");
        if (openTabId.equals("") || openTabId == null) {
            allParams.put("size", 3);
        } else {
            allParams.put("size", 12);
        }
        JSONObject documents = null;
        JSONObject playlist = null;
        JSONObject tests = null;
        JSONObject videos = null;
        allParams.put("exploreContentPage", "true");
        Scope.Params.current().put("exploreContentPage", "true");
        if ("DOCUMENTS".equals(openTabId)) {
            documents = fetchDocumentsService(allParams);
        } else if ("PLAYLISTS".equals(openTabId)) {
            playlist = fetchPlaylistService(allParams);
        } else if ("TESTS".equals(openTabId)) {
            tests = fetchTestService(allParams);
        } else if ("VIDEOS".equals(openTabId)) {
            videos = _getVideos(allParams);
        } else {
            tests = fetchTestService(allParams);
            playlist = fetchPlaylistService(allParams);
        }
        String includeName = "MyContents/index.html";
        flash.put("ENTRY", "DIRECT");
        render("Application/myPages.html", includeName, showMyContents, playlist, documents, tests, videos, isExploreContent, openTabId);
    }

    public static void editDocDirect() {
        myContentDirect("Documents");
    }

    public static JSONObject _getVideoInfo(Map<String, Object> allParams) {

        Promise<JSONResponseWrapper> promise = clientWithAllHeaders(ClientUtil.CONTENT_SERVICE_URL + "/videos/getVideo", allParams, null);
        Logger.log4j.info("BEFORE AWAIT : GET MY CONTENTS - VIDEOS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : GET MY CONTENTS - VIDEOS");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        return data;
    }

    public static JSONObject _getEntityRatingsAndFeedback(Map<String, Object> allParams) {

        Promise<JSONResponseWrapper> promise = clientWithAllHeaders(ClientUtil.CONTENT_SERVICE_URL + "/contents/getEntityInfoForApp", allParams, null);
        Logger.log4j.info("BEFORE AWAIT : GET MY CONTENTS - VIDEOS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : GET MY CONTENTS - VIDEOS");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        return data;
    }

    public static JSONObject _getDocumentInfo(Map<String, Object> allParams) {

        Promise<JSONResponseWrapper> promise = clientWithAllHeaders(ClientUtil.CONTENT_SERVICE_URL + "/documents/getDocument", allParams,null);
        Logger.log4j.info("BEFORE AWAIT : GET MY CONTENTS - DOCUMENT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : GET MY CONTENTS - DOCUMENT");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        return data;
    }

    public static JSONObject _getFileInfo(Map<String, Object> allParams) {

        Promise<JSONResponseWrapper> promise = clientWithAllHeaders(ClientUtil.CONTENT_SERVICE_URL + "/files/getFile", allParams,null);
        Logger.log4j.info("BEFORE AWAIT : GET MY CONTENTS - FILE");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : GET MY CONTENTS - FILE");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        return data;
    }

    public static JSONObject _getModuleInfo(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/modules/getModule", allParams);
        Logger.log4j.info("BEFORE AWAIT : GET MY CONTENTS - MODULE");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : GET MY CONTENTS - MODULE");
        JSONObject data = ResponseUtil.checkResponse(getJSON(promise));
        return data;
    }

    public static JSONObject _getModuleSchedules(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/modules/getModuleSchedules", allParams);
        Logger.log4j.info("BEFORE AWAIT : GET MODULE SCHEDULES - MODULE");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : GET MODULE SCHEDULES - MODULE");
        JSONObject data = ResponseUtil.checkResponse(getJSON(promise));
        return data;
    }

    public static JSONObject _getVideoSugg(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/videos/getSimilarVideos", allParams);
        Logger.log4j.info("BEFORE AWAIT : GET MY CONTENTS - VIDEOS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : GET MY CONTENTS - VIDEOS");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        return data;
    }
    public static void videoPage(String id, String orgId, String moduleId) {
        Map<String, Object> allParams = getReqParams();
        JSONObject myOrgInfo = Institute._setOrgParams(orgId);
        JSONObject video = _getVideoInfo(allParams);
        if(moduleId != null || moduleId != "undefined"){
            allParams.put("moduleId", moduleId);
            allParams.put("context.id", moduleId);
            allParams.put("context.type", ClientUtil.Entity.MODULE);
        }
        allParams.put("entity.id", id);
        allParams.put("entity.type", "VIDEO");
        allParams.put("orgId",allParams.get("parent.id"));
        JSONObject ratings = _getEntityRatingsAndFeedback(allParams);
        try {
            Application._markEntityView(id, ClientUtil.Entity.VIDEO);
        } catch (Exception err) {
        }
        if (!StringUtils.isEmpty(orgId)) {
            String includeInstFile = "MyContents/videoPage.html";
            if (moduleId != null) {
                JSONObject nextVideosInfo = getPlaylistVideos(allParams);
                Logger.log4j.info("Getting Module next Videos for moduleId: "+moduleId);
                render("Institute/header.html", includeInstFile , video, ratings ,myOrgInfo, nextVideosInfo, moduleId);
            }
            render("Institute/header.html", includeInstFile, video, ratings, myOrgInfo);
        } else {
            render(video);
        }
    }

    private static JSONObject getPlaylistVideos(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/videos/getPlaylistVideos", allParams);
        Logger.log4j.info("BEFORE AWAIT GET PLAYLIST VIDEOS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT GET PLAYLIST VIDEOS");
        JSONObject videosData = getJSON(promise);
        videosData = Validation.verifyResponse(videosData);
        return videosData;
    }

    public static void docPage(String id, String orgId) {
        Map<String, Object> allParams = getReqParams();
        JSONObject myOrgInfo = Institute._setOrgParams(orgId);
        JSONObject docInfo = _getDocumentInfo(allParams);
        allParams.put("entity.id", id);
        allParams.put("entity.type", "DOCUMENT");
        allParams.put("orgId",orgId);
        JSONObject ratings = _getEntityRatingsAndFeedback(allParams);
        try {
            Application._markEntityView(id, ClientUtil.Entity.DOCUMENT);
        } catch (Exception err) {
        }
        if (!StringUtils.isEmpty(orgId)) {
            String includeInstFile = "MyContents/docPage.html";
            render("Institute/header.html", includeInstFile, docInfo, myOrgInfo , ratings);
        } else {
            render(docInfo);
        }
    }

    public static void filePage(String id, String orgId) {
        Map<String, Object> allParams = getReqParams();
        JSONObject myOrgInfo = Institute._setOrgParams(orgId);
        JSONObject fileInfo = _getFileInfo(allParams);
        try {
            Application._markEntityView(id, ClientUtil.Entity.FILE);
        } catch (Exception err) {
        }
        if (!StringUtils.isEmpty(orgId)) {
            String includeInstFile = "MyContents/filePage.html";
            render("Institute/header.html", includeInstFile, fileInfo, myOrgInfo);
        } else {
            render(fileInfo);
        }
    }

    public static void modulePage(String id, String orgId) {
        Map<String, Object> allParams = getReqParams();
        JSONObject myOrgInfo = Institute._setOrgParams(orgId);
        JSONObject moduleInfo = _getModuleInfo(allParams);
        allParams.put("moduleId",id);
        JSONObject moduleSchedules = _getModuleSchedules(allParams);
        try {
            Application._markEntityView(id, ClientUtil.Entity.MODULE);
        } catch (Exception err) {
        }
        if (!StringUtils.isEmpty(orgId)) {
            String includeInstFile = "MyContents/modulePage.html";
            render("Institute/header.html", includeInstFile, moduleInfo, myOrgInfo , moduleSchedules);
        } else {
            render(moduleInfo);
        }
    }

    public static void videoSugg(String id) {
        Map<String, Object> allParams = getReqParams();
        JSONObject videos = _getVideoSugg(allParams);
        render(videos);
    }

    public static void addRatingAndFeedback(){
        Map<String,Object> allParams=getReqParams();
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/contents/addRatingAndFeedback", allParams);
        Logger.log4j.info("BEFORE AWAIT : ADD RATINGS AND FEEDBACK");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :  ADD RATINGS AND FEEDBACK");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        renderJSON(resp.toString());
    }

    public static void videoPageDirect(String orgId, @Required String id) {
        JSONObject myOrgInfo = null;
        Map<String, Object> allParams = getReqParams();
        JSONObject video = _getVideoInfo(allParams);
        String moduleId = request.params.get("moduleId");
        if (moduleId != null) {
            allParams.put("context.id", moduleId);
            allParams.put("moduleId", moduleId);
            allParams.put("context.type", ClientUtil.Entity.MODULE);
        }
        allParams.put("entity.id", id);
        allParams.put("entity.type", "VIDEO");
        allParams.put("orgId",orgId);
        JSONObject ratings = _getEntityRatingsAndFeedback(allParams);
        try {
            Application._markEntityView(id, ClientUtil.Entity.VIDEO);
        } catch (Exception err) {
        }
        String includeName = "MyContents/videoPage.html";
        String includeInstFile = includeName;
        if (StringUtils.isNotEmpty(orgId)) {
            myOrgInfo = Institute._setOrgParams(orgId);
            includeName = "Institute/header.html";
            if (moduleId != null) {
                JSONObject nextVideosInfo = getPlaylistVideos(allParams);
                Logger.log4j.info("Getting Module next Videos for moduleId: "+moduleId);
                render("Application/myPages.html", includeName,includeInstFile,video, ratings,myOrgInfo, nextVideosInfo, moduleId);
            }
        }
        flash.put("ENTRY", "DIRECT");
        render("Application/myPages.html", video, ratings, includeName, includeInstFile, myOrgInfo);
    }

    public static void docPageDirect(String orgId, @Required String id) {
        JSONObject myOrgInfo = null;
        Map<String, Object> allParams = getReqParams();
        JSONObject docInfo = _getDocumentInfo(allParams);
        allParams.put("entity.id", id);
        allParams.put("entity.type", "DOCUMENT");
        allParams.put("orgId",orgId);
        JSONObject ratings = _getEntityRatingsAndFeedback(allParams);
        try {
            Application._markEntityView(id, ClientUtil.Entity.DOCUMENT);
        } catch (Exception err) {
        }
        String includeName = "MyContents/docPage.html";
        String includeInstFile = includeName;
        if (StringUtils.isNotEmpty(orgId)) {
            myOrgInfo = Institute._setOrgParams(orgId);
            includeName = "Institute/header.html";
        }
        flash.put("ENTRY", "DIRECT");
        render("Application/myPages.html", docInfo, includeName, includeInstFile, myOrgInfo, ratings);
    }

    public static void filePageDirect(String orgId, @Required String id) {
        JSONObject myOrgInfo = null;
        Map<String, Object> allParams = getReqParams();
        JSONObject fileInfo = _getFileInfo(allParams);
        try {
            Application._markEntityView(id, ClientUtil.Entity.FILE);
        } catch (Exception err) {
        }
        String includeName = "MyContents/filePage.html";
        String includeInstFile = includeName;
        if (StringUtils.isNotEmpty(orgId)) {
            myOrgInfo = Institute._setOrgParams(orgId);
            includeName = "Institute/header.html";
        }
        flash.put("ENTRY", "DIRECT");
        render("Application/myPages.html", fileInfo, includeName, includeInstFile, myOrgInfo);
    }

    public static void modulePageDirect(String orgId, @Required String id) {
        JSONObject myOrgInfo = null;
        Map<String, Object> allParams = getReqParams();
        JSONObject moduleInfo = _getModuleInfo(allParams);
        allParams.put("moduleId",id);
        JSONObject moduleSchedules = _getModuleSchedules(allParams);
        try {
            Application._markEntityView(id, ClientUtil.Entity.MODULE);
        } catch (Exception err) {
        }
        String includeName = "MyContents/modulePage.html";
        String includeInstFile = includeName;
        if (StringUtils.isNotEmpty(orgId)) {
            myOrgInfo = Institute._setOrgParams(orgId);
            includeName = "Institute/header.html";
        }
        flash.put("ENTRY", "DIRECT");
        render("Application/myPages.html", moduleInfo, includeName, includeInstFile, myOrgInfo, moduleSchedules);
    }
}
