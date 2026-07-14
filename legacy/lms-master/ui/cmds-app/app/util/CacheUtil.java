/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import static controllers.AbstractUIController.syncCaller;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.cache.Cache;
import pojos.UserOrg;
import uicom.util.ClientUtil;

/**
 *
 * @author ajith
 */
public class CacheUtil {
    private static String _getKey(String userId){
        return "ORGS_OF_USER_"+userId;
    }
    public static Map<String, UserOrg> getAndCacheUserOrgs(String userId){
        String key=_getKey(userId);
        Map<String, UserOrg> userOrgsMap=(Map<String, UserOrg>)Cache.get(key);
        Logger.log4j.info(userOrgsMap);
        try{
            if(userOrgsMap==null){
                userOrgsMap=new HashMap<String, UserOrg>();
                Logger.log4j.info("no orgs of user found...fetching them.");

                JSONObject userOrgsJSONObject=syncCaller(ClientUtil.ORGANIZATION_SERVICE_URL +
                        "/organizations/getAssociatedOrgsOfUser",null);

                JSONArray userOrgsJSONList=userOrgsJSONObject
                        .getJSONObject("result").getJSONArray("list");
                for(int i=0;i<userOrgsJSONList.length();i++){
                    JSONObject orgjson=userOrgsJSONList.getJSONObject(i);
                    UserOrg userOrg=new UserOrg(orgjson.getString("id"), orgjson.getString("name"),
                            orgjson.getString("fullName"),
                            orgjson.getString("type"),orgjson.getString("orgThumbnail"),
                            orgjson.getString("memberId"),orgjson.getString("orgMemberId"),
                            orgjson.getString("firstName"),orgjson.getString("lastName"),
                            orgjson.getString("profile"),orgjson.getString("thumbnail"),
                            orgjson.getString("userState"),orgjson.getString("authType"),
                            orgjson.getBoolean("showClassroomConnect"));

                    if("MANAGER".equals(userOrg.getOrgUserProfile())){
                        boolean isSuperAdmin = _checkIfSuperAdmin(userOrg.getOrgId());
                        JSONObject extraInfo = userOrg.getExtraInfo();
                        try {
                            extraInfo.put("isSuperAdmin", isSuperAdmin);
                        } catch (JSONException ex) {
                        }
                        userOrg.setExtraInfo(extraInfo);
                    }
                    userOrgsMap.put(orgjson.getString("id"), userOrg);
                }
                Cache.safeAdd(key, userOrgsMap,"10mn");
            }
        }catch(Exception e){
            Logger.log4j.error(e.getMessage());
        }
        return userOrgsMap;
    }
    public static void clearCacheUserOrgs(String userId){
        String key=_getKey(userId);
        Cache.safeDelete(key);
    }
    private static boolean _checkIfSuperAdmin(String orgId){
        Map<String, Collection<String>> allParams = new HashMap<String, Collection<String>>();
        allParams.put("orgId", Arrays.asList(orgId));
        JSONObject resp = syncCaller(ClientUtil.ORGANIZATION_SERVICE_URL +
                        "/organizations/checkIfSuperAdmin",allParams);
        try{
            String errorCode=resp.getString("errorCode");
            boolean isSuperAdmin = resp.getJSONObject("result").optBoolean("isSuperAdmin");
            if(StringUtils.isEmpty(errorCode) && isSuperAdmin){
                return true;
            }
        }catch(JSONException error){
        }
        return false;
    }
}
