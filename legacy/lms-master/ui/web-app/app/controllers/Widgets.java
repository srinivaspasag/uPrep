package controllers;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.libs.F;
import play.libs.F.Promise;
import play.mvc.Http.Request;
import play.mvc.Scope;
import play.mvc.With;
import pojos.WebInfo;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;
import uicom.util.Validation;
import uicom.util.WebUtil;

@With(Security.class)
public class Widgets extends AbstractUIController {

    public static void morePLInfo() {
        render();
    }

    public static void moreDocVideoInfo() {
        render();
    }

    public static void uploadImage() {
        Logger.log4j.info("File Upload REQ Came =============================== ");
        JSONObject data = uploadUtil(ClientUtil.CONTENT_SERVICE_URL
                + "/uploads/uploadImage", null, null);
        renderJSON(data.toString());
    }

    //for complete widgets
    public static void popularPLs() {
        Map<String, Object> allParams = getReqParams();
        JSONObject playlists = _getPopularPLs(allParams);
        render(playlists);
    }

    public static void popularDocs() {
        Scope.Params.current().put("excludeTypes", "Video");
        Map<String, Object> allParams = getReqParams();
        JSONObject docs = _getPopularDocs(allParams);
        render(docs);
    }

    public static void popularTests() {
        Map<String, Object> allParams = getReqParams();
        JSONObject tests = _getPopularTests(allParams);
        render(tests);
    }

    public static void frndSuggs() {
        Map<String, Object> allParams = getReqParams();
        JSONObject frnds = _getFrndSuggs(allParams);
        render(frnds);
    }

    public static void connections() {
        Scope.Params.current().put("targetUserId", session.get("userId"));
        Map<String, Object> allParams = getReqParams();
        JSONObject followers = Widgets._getFollowers(allParams);
        JSONObject following = Widgets._getFollowing(allParams);
        render(followers, following);
    }

    public static void otherConnections() {
        Map<String, Object> allParams = getReqParams();
        JSONObject followers = Widgets._getFollowers(allParams);
        JSONObject following = Widgets._getFollowing(allParams);
        render("Widgets/connections.html", followers, following);
    }

    public static void moreFollowing() {
        JSONObject following = Widgets._getFollowing(null);
        JSONArray users = new JSONArray();
        try {
            users = following.getJSONObject("result").getJSONArray("followSet");
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage());
        }
        render("Widgets/people.html", users);
    }

    public static void moreFollowers() {
        JSONObject followers = Widgets._getFollowers(null);
        JSONArray users = new JSONArray();
        try {
            users = followers.getJSONObject("result").getJSONArray("followSet");
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage());
        }
        render("Widgets/people.html", users);
    }

    //for home page Recommendations
    public static void popularPLItems() {
        JSONObject playlists = _getPopularPLs(null);
        render("Playlists/PLItems.html", playlists);
    }

    public static void popularTestItems() {
        JSONObject tests = _getPopularTests(null);
        render("Tests/testItems.html", tests);
    }

    public static void popularDocItems() {
        Scope.Params.current().put("excludeTypes", "Video");
        JSONObject docs = _getPopularDocs(null);
        render("Doc/docItems.html", docs);
    }

    public static void popularVideoItems() {
        Scope.Params.current().put("includeTypes", "Video");
        JSONObject videos = _getPopularVideos(null);
        render("Doc/videoItems.html", videos);
    }

    public static void frndSuggItems() {
        JSONObject frndSuggs = _getFrndSuggs(null);
        render(frndSuggs);
    }

    //related entities
    public static void relatedEntities() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.RECOS_WEB_SERVICE_URL + "/recommendations/getRelatedEntities", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject entities = getJSON(promise);
        render(entities);
    }

    //for comment widget
    public static void commItems() {
        Map<String, Object> allParams = getReqParams();
        JSONObject comments = _getCommItems(allParams);
        render(comments);
    }

    public static void reviewItems() {
        Map<String, Object> allParams = getReqParams();
        JSONObject reviews = _getCommItems(allParams);
        render(reviews);
    }

    public static void replyItems() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/comments/getComments", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject replies = getJSON(promise);
        replies = Validation.verifyResponse(replies);
        render(replies);
    }

    public static void likeComment() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/comments/voteUpComment", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        renderJSON(resp.toString());
    }

    public static void postComment() {
        Scope.Params.current().put("type", "COMMENT");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/comments/addComment", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        renderJSON(resp.toString());
    }

    public static void postReply() {
        Scope.Params.current().put("type", "REPLY");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/comments/addComment", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        renderJSON(resp.toString());
    }

    public static void postReview() {
        Scope.Params.current().put("type", "REVIEW");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/comments/addComment", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        renderJSON(resp.toString());
    }

    //for tagging
    public static void tagging() {
        render();
    }

    //upvote widget for pl ques doc test viedos
    public static void upVoteItem() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.SOCIALS_WEB_SERVICE_URL + "/socials/upVote", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        renderJSON(resp.toString());
    }

    //follow and unfollow entity
    public static void followEntity() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.SOCIALS_WEB_SERVICE_URL + "/socials/follow", null);
        Logger.log4j.info("BEFORE AWAIT - FOLLOW ENTITY");
        await(promise);
        Logger.log4j.info("AFTER AWAIT - FOLLOW ENTITY");
        JSONObject result = getJSON(promise);
        result = Validation.verifyResponse(result);
        renderJSON(result.toString());
    }

    public static void unFollowEntity() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.SOCIALS_WEB_SERVICE_URL + "/socials/unFollow", null);
        Logger.log4j.info("BEFORE AWAIT - FOLLOW ENTITY");
        await(promise);
        Logger.log4j.info("AFTER AWAIT - FOLLOW ENTITY");
        JSONObject result = getJSON(promise);
        result = Validation.verifyResponse(result);
        renderJSON(result.toString());
    }

    //rating common
    //follow and unfollow entity
    public static void rateEntity() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/commons/rateEntity", null);
        Logger.log4j.info("BEFORE AWAIT - FOLLOW ENTITY");
        await(promise);
        Logger.log4j.info("AFTER AWAIT - FOLLOW ENTITY");
        JSONObject result = getJSON(promise);
        result = Validation.verifyResponse(result);
        renderJSON(result.toString());
    }

    public static void getEntityRatingInfo() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/commons/getEntityRatingInfo", null);
        Logger.log4j.info("BEFORE AWAIT - FOLLOW ENTITY");
        await(promise);
        Logger.log4j.info("AFTER AWAIT - FOLLOW ENTITY");
        JSONObject result = getJSON(promise);
        result = Validation.verifyResponse(result);
        renderJSON(result.toString());
    }

    //sharing
    public static void sharedContent() {
        String sharedUrl = "getSharedEntity";
        String sharedType = Scope.Params.current().get("sharedType");
        if (sharedType != null && sharedType.equals("BY_ME")) {
            sharedUrl = "getSharedEntityByMe";
        }
        F.Promise<JSONResponseWrapper> promise
                = client(uicom.util.ClientUtil.LIB_WEB_SERVICE_URL + "/share/" + sharedUrl, null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject sharedContent = getJSON(promise);
        sharedContent = Validation.verifyResponse(sharedContent);
        render(sharedContent);
    }

    public static void getSharedWithInfo() {
        F.Promise<JSONResponseWrapper> promise
                = client(uicom.util.ClientUtil.LIB_WEB_SERVICE_URL + "/share/getSharedWithInfo", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject shareInfo = getJSON(promise);
        shareInfo = Validation.verifyResponse(shareInfo);
        renderJSON(shareInfo.toString());
    }

    //utilities
    public static void fetchUrl(String url, String domain) throws Exception {
        WebInfo webInfo = WebUtil.fetchDataFromLink(url, domain);
        if (webInfo == null) {
            Logger.log4j.error("Not a valid url");
            renderJSON("Not a valid url");
        }
        Logger.log4j.info("web info for url : " + url + " , : " + webInfo);
        renderJSON(webInfo);
    }

    public static void fetchVideoInfo() throws Exception {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/videos/getVideoInfo", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject video = getJSON(promise);
        renderJSON(video.toString());
    }

    //return funtions for widgets
    public static JSONObject _getPopularPLs(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.RECOS_WEB_SERVICE_URL + "/recommendations/getplaylists", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        return resp;
    }

    public static JSONObject _getPopularTests(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.RECOS_WEB_SERVICE_URL + "/recommendations/gettests", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        return resp;
    }

    public static JSONObject _getPopularDocs(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.RECOS_WEB_SERVICE_URL + "/recommendations/getDocuments", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject docs = getJSON(promise);
        return docs;
    }

    public static JSONObject _getPopularVideos(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.RECOS_WEB_SERVICE_URL + "/recommendations/getDocuments", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject videos = getJSON(promise);
        return videos;
    }

    public static JSONObject _getFrndSuggs(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.RECOS_WEB_SERVICE_URL + "/Recommendations/getUserRecommendations", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject frndSuggs = getJSON(promise);
        return frndSuggs;
    }

    public static JSONObject _getFollowers(Map<String, Object> allParams) {
        /*Promise<JSONResponseWrapper> promiseFollowings = client(ClientUtil.SOCIALS_WEB_SERVICE_URL + "/socials/getFollowers",allParams);
         Logger.log4j.info("BEFORE AWAIT");
         await(promiseFollowings);
         Logger.log4j.info("AFTER AWAIT");
         JSONObject followings= getJSON(promiseFollowings);
         return followings;*/
        return _getEntityFollowers(allParams);
    }

    public static JSONObject _getFollowing(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promiseFollowers = client(ClientUtil.SOCIALS_WEB_SERVICE_URL + "/socials/getFollowings", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promiseFollowers);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject followers = getJSON(promiseFollowers);
        followers = Validation.verifyResponse(followers);
        return followers;
    }

    protected static JSONObject _getEntityFollowers(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promiseFollowings = client(ClientUtil.SOCIALS_WEB_SERVICE_URL + "/socials/getFollowers", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promiseFollowings);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject followings = getJSON(promiseFollowings);
        followings = Validation.verifyResponse(followings);
        return followings;
    }

    protected static JSONObject _getEntityUpvoters(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.SOCIALS_WEB_SERVICE_URL + "/socials/getVoters", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject upvoters = getJSON(promise);
        upvoters = Validation.verifyResponse(upvoters);
        return upvoters;
    }

    public static void popupEntityFollowers() {
        JSONObject users = _getEntityFollowers(null);
        render("tags/widgets/popupUsers.html", users);
    }

    public static void popupEntityUpVoters() {
        JSONObject users = _getEntityUpvoters(null);
        render("tags/widgets/popupUsers.html", users);
    }

    public static void tooltipEntityFollowers() {
        JSONObject users = _getEntityFollowers(null);
        render("tags/widgets/toolTipUsers.html", users);
    }

    public static void tooltipEntityUpVoters() {
        JSONObject users = _getEntityUpvoters(null);
        render("tags/widgets/toolTipUsers.html", users);
    }

    public static JSONObject _getEntityCommonFollowing(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promiseFollowers = client(ClientUtil.FOLLOW_WEB_SERVICE_URL + "/Follows"
                + "/getEntityCommonFollowing", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promiseFollowers);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject followers = getJSON(promiseFollowers);
        return followers;
    }

    public static JSONObject _getCommItems(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/comments/getComments", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        return resp;
    }

    public static void markContentCompleted() {
        JSONObject resp = _markContentCompleted(null);
        renderJSON(resp.toString());
    }

    protected static JSONObject _markContentCompleted(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.SOCIALS_WEB_SERVICE_URL + "/socials/completed", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    //deprecated
    public static String morify(String textContent, int charLimit) {
        return textContent;
    }

    public static String getChallengeDuration(int secs) {
        return "-";
    }

    public static final String RESPONSE_RESULT = "result";

    public static void userpoints() {
        render();
    }

    public static void followings() {
        JSONObject followings = _getFollowing(null);
        renderJSON(followings.toString());
    }

    public static void followers() {
        JSONObject followers = _getFollowers(null);
        renderJSON(followers.toString());
    }

    public static void showFollowers() {
        JSONObject resp = new JSONObject();
        try {
            resp.put("people", _getFollowers(null).getJSONObject("result"));
        } catch (Exception e) {
            Logger.log4j.info("problem in extracting followers" + e);
        }
        String entityId = Request.current().params.get("targetUserId");
        String status = Request.current().params.get("status");
        String heading = "Followers";
        String className = "showFollowers";
        render("/Widgets/peoplePopup.html", resp, status, entityId, heading, className);
    }

    public static void showFollowing() {
        JSONObject resp = new JSONObject();
        try {
            resp = resp.put("people", _getFollowing(null).getJSONObject("result"));
        } catch (Exception e) {
            Logger.log4j.info("problem in extracting followings" + e);
        }
        String entityId = Request.current().params.get("targetUserId");
        String status = Request.current().params.get("status");
        String heading = "Following";
        String className = "showFollowing";
        render("/Widgets/peoplePopup.html", resp, status, entityId, heading, className);
    }

    public static void getFollowStats() {
        Scope.Params.current().put("srcUserId", session.get("userId"));
        Map<String, Object> allParams = getReqParams();
        JSONObject followStats = calcFollowStats(allParams);
        renderJSON(followStats.toString());
    }

    public static JSONObject calcFollowStats(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promiseFollowers = client(ClientUtil.FOLLOW_WEB_SERVICE_URL + "/follows/getFollowerCount", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promiseFollowers);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject flers = getJSON(promiseFollowers);
        Promise<JSONResponseWrapper> promiseFollowings = client(ClientUtil.FOLLOW_WEB_SERVICE_URL + "/follows/getFollowingCount", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promiseFollowings);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject flings = getJSON(promiseFollowings);
        Promise<JSONResponseWrapper> promiseCommon = client(ClientUtil.FOLLOW_WEB_SERVICE_URL + "/follows/getCommonFollowingCount", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promiseCommon);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject common = getJSON(promiseCommon);
        JSONObject isfollow = null;
        JSONObject followStats = new JSONObject();
        try {
            if (flers != null && flers.get("errorMessage").toString().isEmpty()) {
                followStats.put("followers", flers.get("result"));
            }
        } catch (Exception e) {
            Logger.log4j.error("error for appending followers" + e);
        }
        try {
            if (flings != null && flings.get("errorMessage").toString().isEmpty()) {
                followStats.put("followings", flings.get("result"));
            }
        } catch (Exception e) {
            Logger.log4j.error("error for appending followings" + e);
        }
        try {
            if (common != null && common.get("errorMessage").toString().isEmpty()) {
                followStats.put("common", common.get("result"));
            }
        } catch (Exception e) {
            Logger.log4j.error("error for appending common" + e);
        }
        try {
            if (isfollow != null && isfollow.get("errorMessage").toString().isEmpty()) {
                followStats.put("isFollowing", isfollow.get("result"));
            }
        } catch (Exception e) {
            Logger.log4j.error("error for appending following status" + e);
        }
        return followStats;
    }
}
