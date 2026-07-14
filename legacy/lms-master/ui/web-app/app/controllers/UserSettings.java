/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.Map;

import org.json.JSONObject;

import play.mvc.With;
import uicom.util.ClientUtil;

/**
 *
 * @author anirban
 */
@With(Security.class)
public class UserSettings extends UIComUserSettings{
    public static void openSettings(){
        Map<String, Object> allParams = getReqParams();
        try{
            Application.recordActivity(ClientUtil.ActivityPages.USER_SETTINGS,ClientUtil.ActivityAction.OPEN,null,null);
        }catch(Exception ex){}
        JSONObject memberInfo = Institute._getMemberInfo(allParams);
        JSONObject userInfo = _getUserInfo(allParams);
        render("/UIComTags/settings.html",memberInfo,userInfo);
    }
}
