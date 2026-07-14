/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;

/**
 *
 * @author ajith
 */
public class UrlFramer {
    public static JSONObject getUserClassAndUrl(JSONObject user,String orgId) throws JSONException{
        JSONObject userClassAndUrl=new JSONObject();
        try{
            String userId=user.getString("id");
            String profile=user.getString("profile").toUpperCase();
            String url="/organization/"+orgId+"/member/"+userId;
            String className="openMemberPage";
            
            if(profile.equals("STUDENT")){
                url="/organization/"+orgId+"/student/"+userId;
                className="openStudentPage";
            }else if(profile.equals("OFFLINE_USER")){
                url="/organization/"+orgId+"/offlineuser/"+userId;
                className="openOfflineUserPage";
            }
            userClassAndUrl.put("url",url);
            userClassAndUrl.put("className",className);             
        }catch(Exception e){
            Logger.log4j.error(e.getMessage(),e);
        }       
        return userClassAndUrl;
    }     
    public static String getProgramUrl(String name,String id,String orgId){
        String url;
        if( StringUtils.isEmpty(name)|| StringUtils.isEmpty(id)){
            url="/organization/"+orgId+"/resources";
        }
        else{
            url="/organization/"+orgId+"/program/"+id;
        }
        return url;
    }        
}
