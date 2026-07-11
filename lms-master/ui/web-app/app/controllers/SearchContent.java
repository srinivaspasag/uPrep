/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.Map;

import org.json.JSONObject;

import play.Logger;
import play.libs.F.Promise;
import play.mvc.Scope;
import uicom.util.ClientUtil;
import uicom.util.Validation;
/**
 *
 * @author anirbandutta
 */
public class SearchContent  extends AbstractUIController{
  
    public static void index(){
        render();
    }
    public static void getFacets(){
        render();
    }
    public static void getSuggestions(){
        String queryStr = Scope.Params.current().get("query");
        String entityName = Scope.Params.current().get("entityName");
        JSONObject suggestions = _getSuggestions(null);
        suggestions = Validation.verifyResponse(suggestions);
        render(suggestions,queryStr,entityName);
    }
    public static void getSearchResult(){
        JSONObject searchResponse=_getSearchResponse(null);
        searchResponse = Validation.verifyResponse(searchResponse);
        String subResponseType = Scope.Params.current().get("includeTypes[0]");
        String eType = Scope.Params.current().get("eType");
        JSONObject frnds = null;
        Map<String,Object> allParams=getReqParams();
        if("USER".equals(eType)){
            frnds = Widgets._getFrndSuggs(allParams);
        }
        String[] brdTypes = {"targets","courses","topics","subTopics","tags"}; 
        String[] brdNames = {"Target Exam","Subjects","Topics","Sub Topics","Related Tags"};
        String[] brdTags = {"targetIds","brdIds","brdIds","brdIds","tags"};
        render(searchResponse,subResponseType,brdTypes,brdNames,brdTags,frnds);
    }
    public static void getSearchBody(){
        Scope.Params.current().put("facet","false");
        JSONObject searchResponse=_getSearchResponse(null);
        searchResponse = Validation.verifyResponse(searchResponse);
        String subResponseType = Scope.Params.current().get("includeTypes[0]");
        render(searchResponse,subResponseType);
    }

    public static void searchDirect(String query){
        Map<String,Object> allParams=getReqParams();
        allParams.put("start", 0);
        allParams.put("size", 10);
        allParams.put("facet", true);        
        JSONObject searchResponse=_getSearchResponse(allParams);
        searchResponse = Validation.verifyResponse(searchResponse);
        String subResponseType = Scope.Params.current().get("includeTypes[0]");
        String eType = Scope.Params.current().get("eType");
        JSONObject frnds = null;
        allParams=getReqParams();
        if("USER".equals(eType)){
            frnds = Widgets._getFrndSuggs(allParams);
        }
        String[] brdTypes = {"targets","courses","topics","subTopics","tags"}; 
        String[] brdNames = {"Target Exam","Subjects","Topics","Sub Topics","Related Tags"};
        String[] brdTags = {"targetIds","brdIds","brdIds","brdIds","tags"};
        flash.put("ENTRY", "DIRECT");
        String includeName="SearchContent/getSearchResult.html";
        render("Application/myPages.html",includeName,searchResponse,subResponseType,brdTypes,brdNames,brdTags,frnds);
    }
    
    
    
    public static JSONObject _getSearchResponse(Map<String, Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.SEARCH_WEB_SERVICE_URL +"/search/search",allParams);
        Logger.log4j.info("BEFORE AWAIT - GET SEARCH RESPONSE");
        await(promise);
        Logger.log4j.info("AFTER AWAIT - GET SEARCH RESPONSE");
        JSONObject resp = getJSON(promise);
        return resp;
    }  
    public static JSONObject _getSuggestions(Map<String,Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.SEARCH_WEB_SERVICE_URL +"/search/suggestion",allParams);
        Logger.log4j.info("BEFORE AWAIT - GET SEARCH SUGGESTION");
        await(promise);
        Logger.log4j.info("AFTER AWAIT - GET SEARCH SUGGESTION");
        JSONObject resp = getJSON(promise);
        return resp;
    }        
}
