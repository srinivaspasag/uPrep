/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;

public class TemplateHelper {

    private static final String RESULT_LIST_STR    = "{'list':[{id:'',name:'No Sorting',order:'DESC'},{id:'attempts',name:'Most Attempted',order:'DESC'},{id:'attempts',name:'Least Attempted',order:'ASC'},{id:'correct',name:'Most Correct',order:'DESC'},{id:'correct',name:'Least Correct',order:'ASC'}]}";
    private static final String SORT_TYPE_LIST_STR = "{'list':[{id:'timeCreated',name:'Recently Added'},{id:'views',name:'Most Popular'}]}";

    public static JSONObject _getAsnUrl(JSONObject test, String userRole) {
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
        String url = "/assignment/" + role + "/" + id;
        try {
            data.put("url", url);
            data.put("className", "openInstAssignment");
            data.put("id", id);
            data.put("dataAttrs", "data-test-id=" + id);
        } catch (JSONException ex) {
        }
        return data;
    }

    public static JSONObject _getTestUrl(JSONObject test, String userRole) {
        String url = "";
        String className = "";
        JSONObject data = new JSONObject();
        try {
            String testId = test.getString("id");
            boolean attempted = test.optBoolean("attempted", false);
            if (test.has("type") && "ASSIGNMENT".equals(test.getString("type"))) {
                return _getAsnUrl(test, userRole);
            }
            String mode = "ONLINE";
            if (test.has("mode")) {
                mode = test.getString("mode");
            }
            if ((!attempted) && "ONLINE".equals(mode) && "STUDENT".equals(userRole)) {
                url = "/pretest/" + testId;
                className = "openInstPreTest";
            } else {
                url = "/test/" + testId;
                className = "openInstTest";
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

    public static JSONObject _getVidUrl(JSONObject vid) {
        JSONObject data = new JSONObject();
        String id = "";
        try {
            id = vid.getString("id");
        } catch (JSONException ex) {
        }
        String url = "/video/" + id;
        String className = "openInstVideo";
        try {
            data.put("url", url);
            data.put("className", className);
            data.put("id", id);
            data.put("dataAttrs", "data-video-id=" + id);
        } catch (JSONException ex) {
        }
        return data;
    }

    public static JSONObject _getDocUrl(JSONObject doc) {
        JSONObject data = new JSONObject();
        String id = "";
        try {
            id = doc.getString("id");
        } catch (JSONException ex) {
        }
        String url = "/document/" + id;
        String className = "openInstDoc";
        try {
            data.put("url", url);
            data.put("className", className);
            data.put("id", id);
            data.put("dataAttrs", "data-doc-id=" + id);
        } catch (JSONException ex) {
        }
        return data;
    }

    public static JSONObject _getFileUrl(JSONObject file) {
        JSONObject data = new JSONObject();
        String id = "";
        try {
            id = file.getString("id");
        } catch (JSONException ex) {
        }
        String url = "/file/" + id;
        String className = "openInstFile";
        try {
            data.put("url", url);
            data.put("className", className);
            data.put("id", id);
            data.put("dataAttrs", "data-file-id=" + id);
        } catch (JSONException ex) {
        }
        return data;
    }

    public static JSONObject _getModuleUrl(JSONObject module) {
        JSONObject data = new JSONObject();
        String id = "";
        try {
            id = module.getString("id");
        } catch (JSONException ex) {
        }
        String url = "/module/" + id;
        String className = "openInstModule";
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
        /*
         * String serverTimeZoneOffsetStr =
         * Play.configuration.getProperty("SERVER_TIME_ZONE_OFFSET"); long
         * serverTimeZoneOffset = 0; try{ serverTimeZoneOffset =
         * Long.parseLong(serverTimeZoneOffsetStr); }catch(Exception ex){
         * serverTimeZoneOffset = 0; }
         */
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

    public static JSONObject _getEntityUrl(JSONObject info) {
        JSONObject data = new JSONObject();
        String id = "";
        try {
            id = info.getString("id");
        } catch (JSONException ex) {
        }
        String url = "/file/" + id;
        String className = "openInstFile";
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
            public JSONObject _getEntityUrl(JSONObject info, String userRole) {
                return _getTestUrl(info, userRole);
            }

        },
        DOCUMENT {

            @Override
            public JSONObject _getEntityUrl(JSONObject info, String userRole) {
                return _getDocUrl(info);
            }

        },
        VIDEO {
            @Override
            public JSONObject _getEntityUrl(JSONObject info, String userRole) {
                return _getVidUrl(info);
            }

        },
        FILE {
            @Override
            public JSONObject _getEntityUrl(JSONObject info, String userRole) {
                return _getFileUrl(info);
            }
        },
        ASSIGNMENT {
            @Override
            public JSONObject _getEntityUrl(JSONObject info, String userRole) {
                return _getAsnUrl(info, userRole);
            }
        },
        UNKNOWN {
            @Override
            public JSONObject _getEntityUrl(JSONObject info, String userRole) {
                return null;
            }
        };

        public abstract JSONObject _getEntityUrl(JSONObject info, String userRole);

        public static EntityUrl valueOfKey(String key) {

            EntityUrl entityUrl = UNKNOWN;
            try {
                entityUrl = valueOf(StringUtils.upperCase(key.trim()));
            } catch (Exception e) {
            }
            return entityUrl;
        }
    }
}
