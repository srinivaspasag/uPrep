/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webUtils;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;

/**
 *
 * @author anirban
 */
public class TemplateHelper {

    private static final String RESULT_LIST_STR = "{'list':[{id:'',name:'No Sorting',order:'DESC'},{id:'attempts',name:'Most Attempted',order:'DESC'},{id:'attempts',name:'Least Attempted',order:'ASC'},{id:'correct',name:'Most Correct',order:'DESC'},{id:'correct',name:'Least Correct',order:'ASC'}]}";
    private static final String SORT_TYPE_LIST_STR = "{'list':[{id:'timeCreated',name:'Recently Added'},{id:'views',name:'Most Popular'}]}";

    public static JSONObject _getAsnUrl(JSONObject test, String orgId, String userRole) {
        JSONObject data = new JSONObject();
        String role = "teacher";
        if ("STUDENT".equals(userRole)) {
            role = "student";
        }
        String id = "";
        try {
            id = test.getString("id");
        } catch (JSONException ex) {
        }
        String url = "/organization/" + orgId + "/assignment/" + role + "/" + id;
        try {
            data.put("url", url);
            data.put("className", "openInstAssignment");
            data.put("id", id);
            data.put("dataAttrs", "data-test-id=" + id);
        } catch (JSONException ex) {
        }
        return data;
    }

    public static JSONObject _getTestUrl(JSONObject test, String orgId, String userRole) {
        String url = "";
        String className = "";
        JSONObject data = new JSONObject();
        try {
            String testId = test.getString("id");
            boolean attempted = test.optBoolean("attempted", false);
            //boolean completed = Boolean.getBoolean(test.getString("attempted"));
            if (test.has("type") && "ASSIGNMENT".equals(test.getString("type"))) {
                return _getAsnUrl(test, orgId, userRole);
            }
            String mode = "ONLINE";
            if (test.has("mode")) {
                mode = test.getString("mode");
            }
            if (orgId != null && !orgId.isEmpty()) {
                if ((!attempted) && "ONLINE".equals(mode) && "STUDENT".equals(userRole)) {
                    url = "/organization/" + orgId + "/pretest/" + testId;
                    className = "openInstPreTest";
                } else {
                    url = "/organization/" + orgId + "/test/" + testId;
                    className = "openInstTest";
                }
            } else {
                url = "/test/" + testId;
                className = "openTestPage";
            }
            data.put("url", url);
            data.put("className", className);
            data.put("id", testId);
            data.put("dataAttrs", "data-test-id=" + testId);
        } catch (JSONException e) {
            Logger.log4j.error("Error in preparing test url" + e.getMessage());
        }
        return data;
    }

    public static JSONObject _getVidUrl(JSONObject vid, String orgId) {
        JSONObject data = new JSONObject();
        String id = "";
        try {
            id = vid.getString("id");
        } catch (JSONException ex) {
        }
        String className = "openDocPage";
        String url = "/video/" + id;
        if (!StringUtils.isEmpty(orgId)) {
            url = "/organization/" + orgId + "/video/" + id;
            className = "openInstVideo";
        }
        try {
            data.put("url", url);
            data.put("className", className);
            data.put("id", id);
            data.put("dataAttrs", "data-video-id=" + id);
        } catch (JSONException ex) {
        }
        return data;
    }

    public static JSONObject _getDocUrl(JSONObject doc, String orgId) {
        JSONObject data = new JSONObject();
        String id = "";
        try {
            id = doc.getString("id");
        } catch (JSONException ex) {
        }
        String className = "openDocPage";
        String url = "/document/" + id;
        if (!StringUtils.isEmpty(orgId)) {
            url = "/organization/" + orgId + "/document/" + id;
            className = "openInstDoc";
        }
        try {
            data.put("url", url);
            data.put("className", className);
            data.put("id", id);
            data.put("dataAttrs", "data-doc-id=" + id);
        } catch (JSONException ex) {
        }
        return data;
    }

    public static JSONObject _getFileUrl(JSONObject file, String orgId) {
        JSONObject data = new JSONObject();
        String id = "";
        try {
            id = file.getString("id");
        } catch (JSONException ex) {
        }
        String className = "openFilePage";
        String url = "/file/" + id;
        if (!StringUtils.isEmpty(orgId)) {
            url = "/organization/" + orgId + "/file/" + id;
            className = "openInstFile";
        }
        try {
            data.put("url", url);
            data.put("className", className);
            data.put("id", id);
            data.put("dataAttrs", "data-file-id=" + id);
        } catch (JSONException ex) {
        }
        return data;
    }

    public static JSONObject _getModuleUrl(JSONObject module, String orgId) {
        JSONObject data = new JSONObject();
        String id = "";
        try {
            id = module.getString("id");
        } catch (JSONException ex) {
        }
        String className = "openModulePage";
        String url = "/module/" + id;
        if (!StringUtils.isEmpty(orgId)) {
            url = "/organization/" + orgId + "/module/" + id;
            className = "openInstModule";
        }
        try {
            data.put("url", url);
            data.put("className", className);
            data.put("id", id);
        } catch (JSONException ex) {
        }
        return data;
    }

    public static JSONObject _getParamsResultTypeList() {
        JSONObject resultTypeList = null;
        try {
            resultTypeList = new JSONObject(RESULT_LIST_STR);
        } catch (JSONException ex) {
        }
        return resultTypeList;
    }

    public static JSONObject _getParamsSortTypeList() {
        JSONObject resultTypeList = null;
        try {
            resultTypeList = new JSONObject(SORT_TYPE_LIST_STR);
        } catch (JSONException ex) {
        }
        return resultTypeList;
    }

    public static long _getClientTimeOffsetCalc(long curTime, String clientOffsetStr) {
        /*String serverTimeZoneOffsetStr = Play.configuration.getProperty("SERVER_TIME_ZONE_OFFSET");
         long serverTimeZoneOffset = 0;
         try{
         serverTimeZoneOffset = Long.parseLong(serverTimeZoneOffsetStr);
         }catch(Exception ex){
         serverTimeZoneOffset = 0;
         }*/
        long clientOffset = 0;
        try {
            clientOffset = Long.parseLong(clientOffsetStr);
        } catch (Exception ex) {
            clientOffset = 0;
        }
        curTime = curTime - clientOffset;
        return curTime;
    }

    public static boolean isNull(JSONObject j) {
        boolean ret = false;
        try {
            if (j == null || j.equals(null)) {
                ret = true;
            }
        } catch (Exception er) {
            ret = true;
        }
        return ret;
    }

    public static boolean isNull(JSONObject j, String key) {
        boolean ret = false;
        try {
            if (j.has(key) && (j.get(key) == null || j.get(key).equals(null))) {
                ret = true;
            }
        } catch (Exception er) {
            ret = true;
        }
        return ret;
    }

    public static boolean isNull(String s) {
        boolean ret = false;
        try {
            if (s == null || "null".equals(s)) {
                ret = true;
            }
        } catch (Exception er) {
            ret = true;
        }
        return ret;
    }

    public static JSONObject _getEntityUrl(JSONObject info, String orgId) {
        JSONObject data = new JSONObject();
        String id = "";
        try {
            id = info.getString("id");
        } catch (JSONException ex) {
        }
        String className = "openFilePage";
        String url = "/file/" + id;
        if (!StringUtils.isEmpty(orgId)) {
            url = "/organization/" + orgId + "/file/" + id;
            className = "openInstFile";
        }
        try {
            data.put("url", url);
            data.put("className", className);
            data.put("id", id);
        } catch (JSONException ex) {
        }
        return data;
    }

    public enum EntityUrl {

        TEST {

                    @Override
                    public JSONObject _getEntityUrl(JSONObject info, String orgId, String userRole) {
                        return _getTestUrl(info, orgId, userRole);
                    }

                }, DOCUMENT {

                    @Override
                    public JSONObject _getEntityUrl(JSONObject info, String orgId, String userRole) {
                        return _getDocUrl(info, orgId);
                    }

                }, VIDEO {
                    @Override
                    public JSONObject _getEntityUrl(JSONObject info, String orgId, String userRole) {
                        return _getVidUrl(info, orgId);
                    }

                }, FILE {
                    @Override
                    public JSONObject _getEntityUrl(JSONObject info, String orgId, String userRole) {
                        return _getFileUrl(info, orgId);
                    }
                }, ASSIGNMENT {
                    @Override
                    public JSONObject _getEntityUrl(JSONObject info, String orgId, String userRole) {
                        return _getAsnUrl(info, orgId, userRole);
                    }
                }, UNKNOWN {
                    @Override
                    public JSONObject _getEntityUrl(JSONObject info, String orgId, String userRole) {
                        return null;
                    }
                };

        public abstract JSONObject _getEntityUrl(JSONObject info, String orgId, String userRole);

        public static EntityUrl valueOfKey(String key) {

            EntityUrl entityUrl = UNKNOWN;
            try {
                entityUrl = valueOf(StringUtils.upperCase(key
                        .trim()));
            } catch (Exception e) {
            }
            return entityUrl;
        }
    }
}
