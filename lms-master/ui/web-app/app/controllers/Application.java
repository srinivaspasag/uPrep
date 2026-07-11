package controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Play;
import play.cache.Cache;
import play.libs.F.Promise;
import play.mvc.Scope;
import play.mvc.With;
import uicom.util.ClientUtil;
import uicom.util.Validation;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.ning.http.multipart.FilePart;

@With(Security.class)
public class Application extends AbstractUIController {

    private enum EntityType {
        HIGHLIGHT, DOC, PAGE;
    }

    public static void googleCharts() {

        render();
    }

    public static void fe() {

        render();
    }

    public static void result() {

        render();
    }

    public static void gets() {

        renderText(session.get("userId"));
    }

    public static void index() throws JSONException {

        JSONObject errorCheck = null;
        errorCheck = ErrorStore.checkRequiredParams();
        if (errorCheck != null) {
            render("Application/error.html", errorCheck);
        }

        JSONObject j = null;
        String s = "{'errorMessage':'','result':{'_id':98465394865t94397,'agentOf':'INSTITUTE','firstName':'ajith','lastName':'reddy'},'action':'','errorCode':''}";
        j = new JSONObject(s);
        // JsonElement je=new JsonParser().parse(j.toString());
        // JsonObject jkk=je.getAsJsonObject();
        render(j);
    }

    public static void rte() {

        render();
    }

    public static void chat() {

        render();
    }

    public static void respCheck() throws Exception {

        JSONObject errorCheck = null;
        errorCheck = ErrorStore.checkRequiredParams();
        if (errorCheck != null) {
            renderJSON(errorCheck.toString());
        }
        String s = "{'errorMessage':'','result':{'optionDistributions':[{'count':3,'option':['1']},{'count':6,'option':['2']},{'count':10,'option':['3']},{'count':1,'option':['4']}],'correctAnswer':['4'],'correctAttemptCount':0,'attemptCount':20,'commonPeopleResponse':{'users':[],'totalResults':0},'totalAttempts':20},'errorCode':'COME_HOME'}";
        JSONObject j = new JSONObject(s);
        if (j == null || !j.getString("errorCode").isEmpty()) {
            errorCheck = ErrorStore.getJSONReqError(j);
            renderJSON(errorCheck.toString());
        }
        renderJSON(j.toString());
    }

    public static void entrySuggs() throws Exception {

        String s = "{'errorMessage':'','result':{'boards':[{'name':'IIT JEE','brdId':'e872rqi5e3q'}"
                + ",{'name':'AIEEE','brdId':'es872rqi5e3q'}]},'action':'','errorCode':''}";
        JSONObject j = new JSONObject(s);
        renderJSON(j.toString());
    }

    public static void home() {

        Logger.log4j.info("iam in home");
        render();
    }

    public static void homecontent() {

        render();
    }

    // Feeds

    public static void newsFeeds() {

        String feedType = Scope.Params.current().get("feedType");
        String url = "getNewsFeeds";
        if (feedType.equals("OLD")) {
            url = "getOlderNewsFeeds";
        }
        JSONObject feeds = _getNewsFeeds(null, url);
        feedType = "NEWS_FEEDS";
        render("Application/feeds.html", feeds, feedType);
    }

    public static void notifications() {

        String feedType = Scope.Params.current().get("feedType");
        String url = "getNotifications";
        if (feedType.equals("OLD")) {
            url = "getOlderNotifications";
        }
        JSONObject feeds = _getNotifications(null, url);
        feedType = "NOTIFICATIONS";
        render("Application/feeds.html", feeds, feedType);
    }

    public static void activityFeeds() throws JSONException {

        String feedType = Scope.Params.current().get("feedType");
        String url = "getActivityFeeds";
        if (feedType.equals("OLD")) {
            url = "getOlderActivityFeeds";
        }
        JSONObject feeds = _getActivityFeeds(null, url);
        String s = "{'errorMessage':'','result':{'newsFeedCount':7,'newsFeeds':[{'time':1353928450894,'newsActivityId':'4f43a999dbc450ed18b9da3d_u_9223370682926324913_8277','eType':'FOLLOW_ENTITY','newsFeedId':'4ffd9fcb6f6650edc551f744_o_9223370682926324913_65370','actor':{'id':'4f43a999dbc450ed18b9da3d','lastName':'Kotuli','_id':'4f43a999dbc450ed18b9da3d','profilePic':'http://img.staging.vedantu.com/viewer/view/user/img/9edcec08faf3488fbdaf3d6e3542fc67.usr.img.conv.small.jpg','type':'USER','firstName':'Shankar'},'src':{'id':'50b30d52178a50edc7ac1059','title':'my testing','contentSrc':{'id':'4ffd9fcb6f6650edc551f744','type':'ORGANIZATION'},'desc':'testing','userId':'5028b6c08c8850ed50443b2b','upVote':0,'timeCreated':1353911634134,'type':'DISCUSSION','avgRating':0,'voted':false,'comments':1,'boards':[{'name':'Physics','code':'Physics','type':'COURSE','brdId':'506949e3ff3150eda90fcd7f'},{'name':'Centre Of Mass, Linear Momentum And Collision','code':'Centre Of Mass, Linear Momentum And Collision','type':'TOPIC','brdId':'506949e3ff3150edcd0fcd7f'}]},'sendNewsFeed':true,'srcOwner':{'id':'5028b6c08c8850ed50443b2b','lastName':'patil','_id':'5028b6c08c8850ed50443b2b','profilePic':'http://img.staging.vedantu.com/viewer/view/user/img/default-profile-photo.usr.img.conv.small.jpg','type':'USER','firstName':'vikram'},'why':'INVOLVED','info':{'className':'com.vedantu.news.info.FollowInfo','actionType':'FOLLOWED'}},{'involved':[{'id':'4f43a999dbc450ed18b9da3d','lastName':'Kotuli','_id':'4f43a999dbc450ed18b9da3d','profilePic':'http://img.staging.vedantu.com/viewer/view/user/img/9edcec08faf3488fbdaf3d6e3542fc67.usr.img.conv.small.jpg','type':'USER','firstName':'Shankar'}],'time':1353927466693,'newsActivityId':'4f43a999dbc450ed18b9da3d_u_9223370682927309114_64080','eType':'COMMENT','newsFeedId':'4ffd9fcb6f6650edc551f744_o_9223370682927309114_76162','actor':{'id':'4f43a999dbc450ed18b9da3d','lastName':'Kotuli','_id':'4f43a999dbc450ed18b9da3d','profilePic':'http://img.staging.vedantu.com/viewer/view/user/img/9edcec08faf3488fbdaf3d6e3542fc67.usr.img.conv.small.jpg','type':'USER','firstName':'Shankar'},'src':{'id':'50b332938b5550ed89073a36','comm':{'tags':[],'textContent':'This is reply for testung<br>','replys':0,'rootId':'50b332938b5550ed89073a36','rootType':'DISCUSSION','type':'DISCUSSION','id':'50b332938b5550ed89073a36','voteUp':0,'pageNumber':0,'commId':'50b34b2ad2ea50ed4360e257','rating':0,'voted':false,'isReply':false},'pCommId':'50b34b1dd2ea50ed4160e257','pComm':{'tags':[],'textContent':'This is a comment for testing<br>','replys':1,'rootId':'50b332938b5550ed89073a36','rootType':'DISCUSSION','type':'DISCUSSION','id':'50b332938b5550ed89073a36','voteUp':0,'pageNumber':0,'commId':'50b34b1dd2ea50ed4160e257','rating':0,'voted':false,'isReply':false},'pDoc':{'id':'50b332938b5550ed89073a36','title':'sadada','contentSrc':{'id':'4ffd9fcb6f6650edc551f744','type':'ORGANIZATION'},'desc':'asdasd','userId':'4f43a999dbc450ed18b9da3d','upVote':1,'timeCreated':1353921171316,'type':'DISCUSSION','avgRating':0,'voted':false,'comments':1,'boards':[{'name':'Centre Of Mass, Linear Momentum And Collision','code':'Centre Of Mass, Linear Momentum And Collision','type':'TOPIC','brdId':'506949e3ff3150edcd0fcd7f'},{'name':'Physics','code':'Physics','type':'COURSE','brdId':'506949e3ff3150eda90fcd7f'}]},'commId':'50b34b2ad2ea50ed4360e257','pCommBy':{'id':'4f43a999dbc450ed18b9da3d','lastName':'Kotuli','_id':'4f43a999dbc450ed18b9da3d','profilePic':'http://img.staging.vedantu.com/viewer/view/user/img/9edcec08faf3488fbdaf3d6e3542fc67.usr.img.conv.small.jpg','type':'USER','firstName':'Shankar'},'type':'DISCUSSION'},'sendNewsFeed':true,'srcOwner':{'id':'4f43a999dbc450ed18b9da3d','lastName':'Kotuli','_id':'4f43a999dbc450ed18b9da3d','profilePic':'http://img.staging.vedantu.com/viewer/view/user/img/9edcec08faf3488fbdaf3d6e3542fc67.usr.img.conv.small.jpg','type':'USER','firstName':'Shankar'},'comments':'This is reply for testung<br>','why':'INVOLVED','info':{'className':'com.vedantu.news.info.CommentInfo','actionType':'COMMENTED'}},{'involved':[{'id':'4f43a999dbc450ed18b9da3d','lastName':'Kotuli','_id':'4f43a999dbc450ed18b9da3d','profilePic':'http://img.staging.vedantu.com/viewer/view/user/img/9edcec08faf3488fbdaf3d6e3542fc67.usr.img.conv.small.jpg','type':'USER','firstName':'Shankar'}],'time':1353927453984,'newsActivityId':'4f43a999dbc450ed18b9da3d_u_9223370682927321823_2050','eType':'COMMENT','newsFeedId':'4ffd9fcb6f6650edc551f744_o_9223370682927321823_57370','actor':{'id':'4f43a999dbc450ed18b9da3d','lastName':'Kotuli','_id':'4f43a999dbc450ed18b9da3d','profilePic':'http://img.staging.vedantu.com/viewer/view/user/img/9edcec08faf3488fbdaf3d6e3542fc67.usr.img.conv.small.jpg','type':'USER','firstName':'Shankar'},'src':{'id':'50b332938b5550ed89073a36','comm':{'tags':[],'textContent':'This is a comment for testing<br>','replys':1,'rootId':'50b332938b5550ed89073a36','rootType':'DISCUSSION','type':'DISCUSSION','id':'50b332938b5550ed89073a36','voteUp':0,'pageNumber':0,'commId':'50b34b1dd2ea50ed4160e257','rating':0,'voted':false,'isReply':false},'pCommId':'','pDoc':{'id':'50b332938b5550ed89073a36','title':'sadada','contentSrc':{'id':'4ffd9fcb6f6650edc551f744','type':'ORGANIZATION'},'desc':'asdasd','userId':'4f43a999dbc450ed18b9da3d','upVote':1,'timeCreated':1353921171316,'type':'DISCUSSION','avgRating':0,'voted':false,'comments':1,'boards':[{'name':'Centre Of Mass, Linear Momentum And Collision','code':'Centre Of Mass, Linear Momentum And Collision','type':'TOPIC','brdId':'506949e3ff3150edcd0fcd7f'},{'name':'Physics','code':'Physics','type':'COURSE','brdId':'506949e3ff3150eda90fcd7f'}]},'commId':'50b34b1dd2ea50ed4160e257','type':'DISCUSSION'},'sendNewsFeed':true,'srcOwner':{'id':'4f43a999dbc450ed18b9da3d','lastName':'Kotuli','_id':'4f43a999dbc450ed18b9da3d','profilePic':'http://img.staging.vedantu.com/viewer/view/user/img/9edcec08faf3488fbdaf3d6e3542fc67.usr.img.conv.small.jpg','type':'USER','firstName':'Shankar'},'comments':'This is a comment for testing<br>','why':'INVOLVED','info':{'className':'com.vedantu.news.info.CommentInfo','actionType':'COMMENTED'}},{'time':1353927429020,'newsActivityId':'4f43a999dbc450ed18b9da3d_u_9223370682927346787_99748','eType':'VOTE_ENTITY','newsFeedId':'4ffd9fcb6f6650edc551f744_o_9223370682927346787_91906','actor':{'id':'4f43a999dbc450ed18b9da3d','lastName':'Kotuli','_id':'4f43a999dbc450ed18b9da3d','profilePic':'http://img.staging.vedantu.com/viewer/view/user/img/9edcec08faf3488fbdaf3d6e3542fc67.usr.img.conv.small.jpg','type':'USER','firstName':'Shankar'},'src':{'id':'50b332938b5550ed89073a36','title':'sadada','contentSrc':{'id':'4ffd9fcb6f6650edc551f744','type':'ORGANIZATION'},'desc':'asdasd','userId':'4f43a999dbc450ed18b9da3d','upVote':1,'timeCreated':1353921171316,'type':'DISCUSSION','avgRating':0,'voted':false,'comments':1,'boards':[{'name':'Centre Of Mass, Linear Momentum And Collision','code':'Centre Of Mass, Linear Momentum And Collision','type':'TOPIC','brdId':'506949e3ff3150edcd0fcd7f'},{'name':'Physics','code':'Physics','type':'COURSE','brdId':'506949e3ff3150eda90fcd7f'}]},'sendNewsFeed':true,'srcOwner':{'id':'4f43a999dbc450ed18b9da3d','lastName':'Kotuli','_id':'4f43a999dbc450ed18b9da3d','profilePic':'http://img.staging.vedantu.com/viewer/view/user/img/9edcec08faf3488fbdaf3d6e3542fc67.usr.img.conv.small.jpg','type':'USER','firstName':'Shankar'},'why':'INVOLVED','info':{'className':'com.vedantu.news.info.VoteInfo','actionType':'VOTED'}},{'time':1353921171355,'newsActivityId':'4f43a999dbc450ed18b9da3d_u_9223370682933604452_77568','eType':'INDEX_DISCUSSION','newsFeedId':'4ffd9fcb6f6650edc551f744_o_9223370682933604452_71235','actor':{'id':'4f43a999dbc450ed18b9da3d','lastName':'Kotuli','_id':'4f43a999dbc450ed18b9da3d','profilePic':'http://img.staging.vedantu.com/viewer/view/user/img/9edcec08faf3488fbdaf3d6e3542fc67.usr.img.conv.small.jpg','type':'USER','firstName':'Shankar'},'src':{'id':'50b332938b5550ed89073a36','title':'sadada','contentSrc':{'id':'4ffd9fcb6f6650edc551f744','type':'ORGANIZATION'},'desc':'asdasd','userId':'4f43a999dbc450ed18b9da3d','upVote':1,'timeCreated':1353921171316,'type':'DISCUSSION','avgRating':0,'voted':false,'comments':1,'boards':[{'name':'Centre Of Mass, Linear Momentum And Collision','code':'Centre Of Mass, Linear Momentum And Collision','type':'TOPIC','brdId':'506949e3ff3150edcd0fcd7f'},{'name':'Physics','code':'Physics','type':'COURSE','brdId':'506949e3ff3150eda90fcd7f'}]},'sendNewsFeed':true,'srcOwner':{'id':'4f43a999dbc450ed18b9da3d','lastName':'Kotuli','_id':'4f43a999dbc450ed18b9da3d','profilePic':'http://img.staging.vedantu.com/viewer/view/user/img/9edcec08faf3488fbdaf3d6e3542fc67.usr.img.conv.small.jpg','type':'USER','firstName':'Shankar'},'why':'INVOLVED','info':{'className':'com.vedantu.news.info.EntityNewsInfo','actionType':'ADDED'}},{'involved':[{'id':'4f43a999dbc450ed18b9da3d','lastName':'Kotuli','_id':'4f43a999dbc450ed18b9da3d','profilePic':'http://img.staging.vedantu.com/viewer/view/user/img/9edcec08faf3488fbdaf3d6e3542fc67.usr.img.conv.small.jpg','type':'USER','firstName':'Shankar'}],'time':1353918359110,'newsActivityId':'4f43a999dbc450ed18b9da3d_u_9223370682936416697_37846','eType':'COMMENT','newsFeedId':'4ffd9fcb6f6650edc551f744_o_9223370682936416697_92979','actor':{'id':'4f43a999dbc450ed18b9da3d','lastName':'Kotuli','_id':'4f43a999dbc450ed18b9da3d','profilePic':'http://img.staging.vedantu.com/viewer/view/user/img/9edcec08faf3488fbdaf3d6e3542fc67.usr.img.conv.small.jpg','type':'USER','firstName':'Shankar'},'src':{'id':'50b30d52178a50edc7ac1059','comm':{'tags':[],'textContent':'h<sub>i<\\/sub>','replys':0,'rootId':'50b30d52178a50edc7ac1059','rootType':'DISCUSSION','type':'DISCUSSION','id':'50b30d52178a50edc7ac1059','voteUp':0,'pageNumber':0,'commId':'50b32797d39250ed1045806b','rating':0,'voted':false,'isReply':false},'pCommId':'','pDoc':{'id':'50b30d52178a50edc7ac1059','title':'my testing','contentSrc':{'id':'4ffd9fcb6f6650edc551f744','type':'ORGANIZATION'},'desc':'testing','userId':'5028b6c08c8850ed50443b2b','upVote':0,'timeCreated':1353911634134,'type':'DISCUSSION','avgRating':0,'voted':false,'comments':1,'boards':[{'name':'Physics','code':'Physics','type':'COURSE','brdId':'506949e3ff3150eda90fcd7f'},{'name':'Centre Of Mass, Linear Momentum And Collision','code':'Centre Of Mass, Linear Momentum And Collision','type':'TOPIC','brdId':'506949e3ff3150edcd0fcd7f'}]},'commId':'50b32797d39250ed1045806b','type':'DISCUSSION'},'sendNewsFeed':true,'srcOwner':{'id':'5028b6c08c8850ed50443b2b','lastName':'patil','_id':'5028b6c08c8850ed50443b2b','profilePic':'http://img.staging.vedantu.com/viewer/view/user/img/default-profile-photo.usr.img.conv.small.jpg','type':'USER','firstName':'vikram'},'comments':'h<sub>i<\\/sub>','why':'INVOLVED','info':{'className':'com.vedantu.news.info.CommentInfo','actionType':'COMMENTED'}},{'involved':[{'id':'5028b6c08c8850ed50443b2b','lastName':'patil','_id':'5028b6c08c8850ed50443b2b','profilePic':'http://img.staging.vedantu.com/viewer/view/user/img/default-profile-photo.usr.img.conv.small.jpg','type':'USER','firstName':'vikram'},{'id':'500688d1d56850edefb1c4a9','lastName':'Jain','_id':'500688d1d56850edefb1c4a9','profilePic':'http://img.staging.vedantu.com/viewer/view/user/img/e2cfb2680a2e45d0aadf4f3e2bf75ef2.usr.img.conv.small.jpg','type':'USER','firstName':'Pulkit'}],'time':1353910727840,'newsActivityId':'5028b6c08c8850ed50443b2b_u_9223370682944047967_37639','eType':'COMMENT','newsFeedId':'4ffd9fcb6f6650edc551f744_o_9223370682944047967_37649','actor':{'id':'5028b6c08c8850ed50443b2b','lastName':'patil','_id':'5028b6c08c8850ed50443b2b','profilePic':'http://img.staging.vedantu.com/viewer/view/user/img/default-profile-photo.usr.img.conv.small.jpg','type':'USER','firstName':'vikram'},'src':{'id':'509a6bbadcfb50ed0f0edbe5','comm':{'tags':[],'textContent':'testing&nbsp;','replys':0,'rootId':'509a6bbadcfb50ed0f0edbe5','rootType':'DISCUSSION','type':'DISCUSSION','id':'509a6bbadcfb50ed0f0edbe5','voteUp':0,'pageNumber':0,'commId':'50b309c7d39250edfd44806b','rating':0,'voted':false,'isReply':false},'pCommId':'','pDoc':{'id':'509a6bbadcfb50ed0f0edbe5','title':'What sort of questions should I ask here?&nbsp;','contentSrc':{'id':'4ffd9fcb6f6650edc551f744','type':'ORGANIZATION'},'desc':'This feature allows students of the Institute to ask and discuss questions among themselves and their teachers. This questions through its answers tries to demonstrate What All can you ask, How and To Whom?','userId':'500688d1d56850edefb1c4a9','upVote':0,'timeCreated':1352297402255,'type':'DISCUSSION','avgRating':0,'voted':false,'comments':2,'boards':[{'name':'Laws Of Motion','code':'Laws Of Motion','type':'TOPIC','brdId':'506949e3ff3150ede50fcd7f'},{'name':'Physics','code':'Physics','type':'COURSE','brdId':'506949e3ff3150eda90fcd7f'}]},'commId':'50b309c7d39250edfd44806b','type':'DISCUSSION'},'sendNewsFeed':true,'srcOwner':{'id':'500688d1d56850edefb1c4a9','lastName':'Jain','_id':'500688d1d56850edefb1c4a9','profilePic':'http://img.staging.vedantu.com/viewer/view/user/img/e2cfb2680a2e45d0aadf4f3e2bf75ef2.usr.img.conv.small.jpg','type':'USER','firstName':'Pulkit'},'comments':'testing&nbsp;','why':'INVOLVED','info':{'className':'com.vedantu.news.info.CommentInfo','actionType':'COMMENTED'}}]},'errorCode':''}";
        // JSONObject feeds=new JSONObject(s);
        feedType = "ACTIVITY_FEEDS";
        render("Application/feeds.html", feeds, feedType);
    }

    public static void getNewNewsFeeds() {

        Promise<JSONResponseWrapper> promise = client(ClientUtil.COMM_SERVICE_URL
                + "/NewsFeeds/getNewNewsFeeds", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject newsFeeds = getJSON(promise);
        renderJSON(newsFeeds.toString());
    }

    protected static JSONObject _getNotificationsSummary() {
//        Logger.log4j.info("Key :: "+Scope.Params.current().get("myUserId")+"_getNotificationsSummary");
//        String resp = Cache.get(Scope.Params.current().get("myUserId")+"_getNotificationsSummary", String.class);
//        if(!StringUtils.isEmpty(resp)){
//            Logger.log4j.info("Served _getNotificationsSummary from Cache");
//            JSONObject notiSummary = getCacheDataInJsonObject(resp);
//            return notiSummary;
//        }else{
            Promise<JSONResponseWrapper> promise = client(ClientUtil.COMM_SERVICE_URL
                    + "/newsFeeds/getNotifcationsSummary", null);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            JSONObject notiSummary = getJSON(promise);
//            if(notiSummary != null)
//                Cache.set(Scope.Params.current().get("myUserId")+"_getNotificationsSummary", notiSummary.toString(), "1h");
            return notiSummary;
//        }
    }

    public static void getNotificationsSummary() {

        JSONObject notiSummary = _getNotificationsSummary();
        renderJSON(notiSummary.toString());
    }

    public static void getNotifications() {

        String feedType = Scope.Params.current().get("feedType");
        String url = "getNotifications";
        if (feedType.equals("OLD")) {
            url = "getOlderNotifications";
        }
        JSONObject notis = _getNotifications(null, url);
        renderJSON(notis.toString());
    }

    public static void getActivityFeeds() {

        String feedType = Scope.Params.current().get("feedType");
        String url = "getActivityFeeds";
        if (feedType.equals("OLD")) {
            url = "getOlderActivityFeeds";
        }
        JSONObject newsFeeds = _getActivityFeeds(null, url);
        renderJSON(newsFeeds.toString());
    }

    protected static JSONObject _getNewsFeeds(Map<String, Object> allParams, String url) {

        Promise<JSONResponseWrapper> promise = client(ClientUtil.COMM_SERVICE_URL + "/newsFeeds/"
                + url, allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        return resp;
    }

    protected static JSONObject _getNotifications(Map<String, Object> allParams, String url) {

        Promise<JSONResponseWrapper> promise = client(ClientUtil.COMM_SERVICE_URL + "/newsFeeds/"
                + url, allParams);

        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        return resp;
    }

    protected static JSONObject _getActivityFeeds(Map<String, Object> allParams, String url) {

        Promise<JSONResponseWrapper> promise = client(ClientUtil.COMM_SERVICE_URL + "/newsFeeds/"
                + url, allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        return resp;
    }

    public static void redirect() {

        render();
    }

    public static void updateClickStream() {

        Map<String, Object> allParams = getReqParams();
        // allParams.put("profile", ClientUtil.CLICK_STREAM_WEB_SERVICE_URL);
        client(ClientUtil.CLICK_STREAM_WEB_SERVICE_URL + "/ClickStreamController/record", allParams);
        Logger.log4j.debug("CLICK STREAM UPDATE - " + allParams);
    }

    public static void guide() {

        render("tags/guide.html");
    }

    public static void guideDirect() {

        flash.put("ENTRY", "DIRECT");
        String includeName = "tags/guide.html";
        render("Application/myPages.html", includeName);
    }

    public static void devTestPage() {

        flash.put("ENTRY", "DIRECT");
        String includeName = "tags/devTest.html";
        render("Application/myPages.html", includeName);
    }

    public static void uploadSchedule() {

        render();
    }

    public static void upload() {

        String UPLOAD_PATH = Play.getFile("").getAbsolutePath() + File.separator + "uploads";
        Logger.log4j.info("starting the upload progress");
        JSONObject jsonResponse = new JSONObject();
        Logger.log4j.info(request.isNew);
        if (request.isNew) {
            FileOutputStream moveTo = null;

            // Another way I used to grab the name of the file
            String filename = "dummyfile.xlsx";

            Logger.info("Absolute on where to send %s", UPLOAD_PATH + File.separator);
            try {

                InputStream data = request.body;
                Logger.log4j.info(data);
                File up = new File(UPLOAD_PATH);
                if (!up.exists()) {
                    Logger.log4j.info("making directory " + up.getAbsolutePath() + ", result is : "
                            + up.mkdir());
                }
                File inputDoc = new File(UPLOAD_PATH + File.separator + filename);
                moveTo = new FileOutputStream(inputDoc);
                IOUtils.copy(data, moveTo);
                moveTo.close();

                String orgs = session.get("orgs");
                String orgId = new JSONArray(orgs).getJSONObject(0).getString("_id");

                AsyncHttpClient asynClient = new AsyncHttpClient();
                Future<Response> response = asynClient
                        .preparePost(
                                ClientUtil.PROFILE_WEB_SERVICE_URL
                                        + "/masterplans/uploadMasterPlan?userId="
                                        + session.get("userId") + "&appId="
                                        + Play.configuration.getProperty("auth.appId") + "&orgId="
                                        + orgId)
                        .addHeader("Content-Type",
                                "multipart/form-data;boundary=randomBoundaryNotInAnyOfParts")
                        .addBodyPart(new FilePart("masterPlanFile", inputDoc)).execute();
                Response r = null;

                try {
                    r = response.get();
                    String rspString = r.getResponseBody();
                    jsonResponse = new JSONObject(rspString);
                    Logger.log4j.info("uploaded doc got deleted from local dir : "
                            + inputDoc.delete());
                    Logger.log4j.info("response for profile pic upload" + jsonResponse);
                } catch (InterruptedException e) {
                    Logger.log4j.error(e.getLocalizedMessage());
                } catch (ExecutionException e) {
                    Logger.log4j.error(e.getLocalizedMessage());
                } catch (JSONException e) {
                    Logger.log4j.error(e.getLocalizedMessage());
                } finally {
                    asynClient.close();
                }

            } catch (Exception ex) {

                Logger.log4j.error(ex.getLocalizedMessage());

                renderJSON("{success: false}");
            }

        }
    }

    protected static JSONObject _markEntityView(String entityId, ClientUtil.Entity entityType) {

        Map<String, Object> allParams = getReqParams();
        allParams.put("entity.type", entityType);
        allParams.put("entity.id", entityId);
        ClientUtil.ActivityPages page = null;
        try {
            page = ClientUtil.ActivityPages.valueOf(entityType.name().toUpperCase());
        } catch (Exception ex) {
            page = ClientUtil.ActivityPages.NEW_ENTITY;
        }
        recordActivity(page, ClientUtil.ActivityAction.VIEW, entityType, entityId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.SOCIALS_WEB_SERVICE_URL
                + "/socials/view", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        return resp;
    }

    public static void testCors() {

        render();
    }
    private static JSONObject _getAppSysInfo() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.WEB_APP_URL
                + "/sysinfo/get", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        return resp;
    }
    public static void getAboutAppPopup(String orgId) throws JSONException{
        JSONObject myOrgInfo = Institute._setOrgParams(orgId);
        String instLogo = myOrgInfo.getString("instLogo");
        instLogo = uicom.util.Utilities.getInstLogo(instLogo);
        JSONObject sysInfo = _getAppSysInfo();
        String buildVer = sysInfo.getJSONObject("result").getString("buildVersion");
        render("UIComTags/aboutApp.html",instLogo,buildVer);
    }
}
