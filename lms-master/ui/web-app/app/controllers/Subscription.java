package controllers;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.data.validation.Required;
import play.libs.F.Promise;
import play.mvc.Scope;
import play.mvc.With;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;


@With(Security.class)
public class Subscription extends AbstractUIController {
    public static void signUp(@Required String orgId){
        JSONObject org = Institute._setOrgParams(orgId);
        if(org == null){
           Questions.home();
	}
	String userRole=null;
        try {
            userRole = org.getString("userRole");
        } catch (JSONException ex) {
            Logger.log4j.error(ex);
        }
        if("FRONT_DESK_USER".equals(userRole)){
            String heading = "Sign Up Form For Test Series";
            JSONObject testSeries = _getTestSeriesList();
            render(heading,testSeries,org);
        }else{
           Questions.home();
        }
    }
    public static void postLogin(JSONArray orgs){
	render(orgs);
    } 
    protected static JSONObject _getTestSeriesList(){
        Map<String,Object> allParams=getReqParams();
        allParams.put("start",ClientUtil.DEFAULT_FETCH_START);
        allParams.put("size","100");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/Tests/getTestSeries", allParams);
        Logger.log4j.info("BEFORE AWAIT : GET TEST SERIES");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : GET TEST SERIES");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
    	return resp;
    }      
    public static void getMemberInfo(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/Tests/getUserInfo", null);
        Logger.log4j.info("BEFORE AWAIT : getMemberInfo");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : getMemberInfo");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
	renderJSON(resp.toString());
    }
    public static void submitSignUp(){
        Scope.Params.current().put("subscribedById", session.get("userId"));
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/Tests/subscribe", null);
        Logger.log4j.info("BEFORE AWAIT : SUBMIT SIGN UP FOR TEST");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : SUBMIT SIGN UP FOR TEST");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
	renderJSON(resp.toString());
    }
    public static void updateSignUp(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/Tests/editSubscribedUser", null);
        Logger.log4j.info("BEFORE AWAIT : SUBMIT SIGN UP FOR TEST");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : SUBMIT SIGN UP FOR TEST");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
	renderJSON(resp.toString());
    }
    protected static JSONObject _getSubscribedTestUser(){
	Scope.Params.current().put("size",ClientUtil.DEFAULT_FETCH_SIZE_50);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/Tests/getSubscribedUsers",null);
        Logger.log4j.info("BEFORE AWAIT : GET TEST USERS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : GET TEST USERS");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
    	return resp;
    }
    protected static JSONObject _getSubscribedTestSeries(Map<String,Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/tests/getSubscribedTestSeries", allParams);
        Logger.log4j.info("BEFORE AWAIT GET TEST INFO");
        await(promise);
        Logger.log4j.info("AFTER AWAIT GET TEST INFO");
        JSONObject data = ResponseUtil.checkResponse(getJSON(promise));
        return data;
    }  
    
    
    //test series page
    public static void getSubscribedTestSeries(){
	JSONObject testSeries = _getSubscribedTestSeries(null);
	renderJSON(testSeries.toString());
    }    
    public static void testSeriesPage(){
        JSONObject testSeriesData = _getTestsInTestSeries(null);
	render(testSeriesData);
    }      
    public static void testSeriesDirect(String testSeriesId){
        /*Map<String,Object> allParams=getReqParams();
        JSONObject testSeriesList =_getSubscribedTestSeries(allParams);
	JSONObject testSeriesData = null;
        try{
            String testSeriesId=testSeriesList.getJSONObject("result")
                    .getJSONArray("testSeries").getJSONObject(0).getString("id");            
            allParams.put("testSeriesId",testSeriesId);
            testSeriesData =_getTestsInTestSeries(allParams);
        }catch(Exception e){
            Logger.log4j.error(e.getMessage());
        }*/
        JSONObject testSeriesData = _getTestsInTestSeries(null);
        String includeName="Subscription/testSeriesPage.html";
        flash.put("ENTRY", "DIRECT");
        render("Application/myPages.html",includeName,testSeriesData);
    }    
//    protected static JSONObject _getTestSeriesList(){
//        JSONObject resp = null;
//        String data = "{'result':{'totalHits':1,'testSeries':[{'name':'test1','id':'50d9a626c584ae84a07ffd9c','orgId':'50694e003579ae84e6977f1d','organizationName':'Lakshya','numChildrenTests':0}]},'errorMessage':'','errorCode':''}";
//        try {
//            resp = new JSONObject(data);
//        } catch (JSONException ex) {
//            java.util.logging.Logger.getLogger(Subscription.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    	return resp;
//    }
    protected static JSONObject _getTestsInTestSeries(Map<String,Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/tests/getTestSeriesDetails", allParams);
        Logger.log4j.info("BEFORE AWAIT GET TEST INFO");
        await(promise);
        Logger.log4j.info("AFTER AWAIT GET TEST INFO");
        JSONObject data = ResponseUtil.checkResponse(getJSON(promise));
        return data;
    }      

    
    public static void getSubscribedMembers(){
	JSONObject users = _getSubscribedTestUser();	
	render("Subscription/members.html",users);	
    }
    public static void signedMembers(@Required String orgId){
	JSONObject org = Institute._setOrgParams(orgId);
        String userRole=null;
        try {
            userRole = org.getString("userRole");
        } catch (JSONException ex) {
            Logger.log4j.error(ex);
        }
        if("FRONT_DESK_USER".equals(userRole)){
            JSONObject testSeries = _getTestSeriesList();
            Scope.Params.current().put("start",ClientUtil.DEFAULT_FETCH_START);
            JSONObject users = _getSubscribedTestUser();	
            flash.put("PAGELOAD", "TRUE");
            render(users,testSeries,org);
        }else{
           Questions.home();
        }
    }
}
