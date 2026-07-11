/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */

package uicom.util;

import static controllers.AbstractUIController.syncCaller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.cache.Cache;
import pojos.OrgTnCInfo;

/**
 * 
 * @author ajithreddy
 */
public class Utilities {

    private final static String[] dayArray = { "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT" };

    public static JSONObject getTimeObjFromMilliSec(Long milliseconds) {

        JSONObject timeObj = new JSONObject();
        try {
            String seconds = String.format("%02d", (milliseconds / 1000) % 60);
            String minutes = String.format("%02d", (milliseconds / (1000 * 60)) % 60);
            String hours = String.format("%02d", (int) (milliseconds / (1000 * 60 * 60)));
            timeObj.put("hrs", hours);
            timeObj.put("mins", minutes);
            timeObj.put("secs", seconds);
        } catch (Exception e) {
            Logger.log4j.error(e.getMessage(), e);
        }
        return timeObj;
    }

    public static String getDateMonthYear(Long milliseconds) {

        Date dt = new Date(milliseconds);
        String format = new SimpleDateFormat("dd'/'MM'/'yyyy").format(dt);
        return format;
    }

    public static String getDateMonthYearTime(Long milliseconds) {

        Date dt = new Date(milliseconds);
        SimpleDateFormat sdf = new SimpleDateFormat("dd'/'MM'/'yyyy hh:mm:ss aa");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
        String format = sdf.format(dt);
        return format;
    }

    public static String getDay(Date dt) {

        String day = "";
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int dy = cal.get(Calendar.DAY_OF_WEEK);
        day = dayArray[dy - 1];
        return day;
    }

    public static String getAvgRating(Double avgRating) {

        String rating = avgRating.toString();
        if (rating.length() > 4) {
            rating = rating.substring(0, 4);
        } else if (rating.equals("0.0")) {
            rating = "0";
        }
        return rating;
    }

    public static String getStarsWidth(Double avgRating) {

        int rating = avgRating.intValue();
        Double left = avgRating - rating;
        Double ratingWidth;

        if (left >= 0.5) {
            ratingWidth = (0.5 + rating) * 11;
        } else {
            ratingWidth = (rating + 0.0) * 11;
        }
        String ratingWidthStr = ratingWidth.toString();
        return ratingWidthStr;
    }

    public static String morify(String textContent, int charLimit) {

        String newContent = textContent;
        if (textContent.length() > charLimit) {
            int i = textContent.indexOf(" ", charLimit);
            String hiddenStr = "";
            if (i != -1) {
                hiddenStr = "<span class='hided' style='display:none'>" + textContent.substring(i)
                        + "</span><a class='moreInfo'> ..more</a>";
            } else {
                i = charLimit;
            }
            newContent = textContent.substring(0, i) + hiddenStr;
        }
        return newContent;
    }

    public static JSONObject getTimeArrFromMiliSec(long milliseconds) {// millisec is time from 1970

        JSONObject timeObj = new JSONObject();
        Date dt = new Date(milliseconds);
        Calendar cl = Calendar.getInstance();
        cl.setTime(dt);
        try {
            String seconds = String.format("%02d", cl.get(Calendar.SECOND));
            String minutes = String.format("%02d", cl.get(Calendar.MINUTE));
            String hours = String.format("%2d", cl.get(Calendar.HOUR));
            timeObj.put("hrs", hours);
            timeObj.put("mins", minutes);
            timeObj.put("secs", seconds);
        } catch (Exception e) {
            Logger.log4j.error(e.getMessage(), e);
        }
        return timeObj;
    }

    public static JSONObject getTimeFromMiliSec(long milliseconds) {// milisec is duration

        JSONObject timeObj = new JSONObject();
        try {
            String seconds = String.format("%02d", (milliseconds / 1000) % 60);
            String minutes = String.format("%02d", (milliseconds / (1000 * 60)) % 60);
            String hours = String.format("%2d", (int) (milliseconds / (1000 * 60 * 60)));
            timeObj.put("hrs", hours);
            timeObj.put("mins", minutes);
            timeObj.put("secs", seconds);
        } catch (Exception e) {
            Logger.log4j.error(e.getMessage(), e);
        }
        return timeObj;
    }

    public static String getDiffFromPresent(Long time) {

        Long diff = new Date().getTime() - time;
        Long hours = (diff / 3600000);
        if (hours > 24) {
            return (hours / 24) + " days";
        } else if (hours < 0) {
            return (diff / 60000) + " mins";
        } else {
            return hours + " hrs";
        }
    }

    public static String getOption(int opt) {

        char ch = 'a';
        if (opt < 1 || opt > 26) {
            return "";
        } else {
            opt--;
            ch = (char) (ch + opt);
            return "(" + ch + ")";
        }
    }

    public static double roundDecimals(double percent) {
        
        DecimalFormat doubleForm = new DecimalFormat("#.##");
        return Double.valueOf(doubleForm.format(percent));
    }
        

    public static JSONObject parseJsonText(String jsonStr) {

        if (jsonStr == null || jsonStr.isEmpty()) {
            return null;
        }
        JSONObject j = null;
        try {
            j = new JSONObject(jsonStr);
        } catch (JSONException ex) {
            Logger.log4j.info("Parse Failed in parseJsonText = " + ex);
            j = null;
        }
        return j;
    }

    public static String getInstLogo(String thumbnailImg) {

        if (thumbnailImg == null || thumbnailImg.isEmpty()) {
            thumbnailImg = ClientUtil.INST_DEFAULT_IMG_PATH;
        }
        return thumbnailImg;
    }

    public static Date getDateObject(String dateStr, String format) {

        DateFormat df = new SimpleDateFormat(format);
        Date dateObj;
        try {
            dateObj = df.parse(dateStr);
        } catch (ParseException e) {
            Logger.log4j.info("error in date parsing" + e.getMessage());
            dateObj = null;
        }
        return dateObj;
    }

    public static Boolean isJSONArrayNotEmpty(JSONObject parent, String key) {

        Boolean isNotEmpty = false;
        try {
            if (parent.has(key) && !parent.isNull(key) && parent.getJSONArray(key).length() > 0) {
                isNotEmpty = true;
            }
        } catch (Exception e) {
            isNotEmpty = false;
            Logger.log4j.info("Exception in checking key :" + key + " in " + parent);
        }
        return isNotEmpty;
    }

    public static float calcPercentage(double up, double down) {

        return _calcPercentage(up, down);
    }

    private static float _calcPercentage(double up, double down) {

        if (down == 0) {
            return 0;
        }
        Float percent = (float) ((up * 100 / down));
        /*
         * Logger.log4j.info("======================================== PERCENT CALC");
         * Logger.log4j.info("up == "+up+" , down == "+down+" , percent === "+percent);
         * if(percent<0){ percent = (float)0; }
         */
        percent = Float.parseFloat(String.format("%.2f", percent));
        return percent;
    }

    public static String putParamInUrl(String url, String paramName, String val) {

        String joinChar = url.contains("?") ? "&" : "?";
        try {
            url += joinChar + paramName + "=" + URLEncoder.encode(val, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            url += joinChar + paramName + "=" + val;
        }
        return url;
    }

    public static OrgTnCInfo _getOrgTnCInfo(String orgId, String userId) {

        String key = "ORG_TNC_INFO_" + orgId + "_" + userId;
        OrgTnCInfo orgTnCInfo = (OrgTnCInfo) Cache.get(key);
        Logger.log4j.info(orgTnCInfo);
        try {
            if (orgTnCInfo == null) {
                Logger.log4j.info("no TnC info found for orgId: " + orgId + " and userId: "
                        + userId);

                JSONObject resp = syncCaller(ClientUtil.ORGANIZATION_SERVICE_URL
                        + "/organizations/getOrganization", null);
                JSONObject orgResp = resp.getJSONObject("result");
                orgTnCInfo = new OrgTnCInfo(orgId, orgResp.getString("latestTnCVersion"),
                        orgResp.getBoolean("needsTnCAcceptance"), userId, orgResp.getString("status"));
                Cache.safeAdd(key, orgTnCInfo, "15mn");
            }
        } catch (Exception e) {
            Logger.log4j.error(e.getMessage());
        }
        return orgTnCInfo;
    }
    public static JSONObject _getDefaultCurrency(){
        Locale local = new Locale("en","IN");
        Locale.setDefault(local);
        String defaultCode = Currency.getInstance(local).getCurrencyCode();
        JSONObject obj = new JSONObject();
        try {
            obj.put("default", true);
            obj.put("code", defaultCode);
            obj.put("country", local.getDisplayCountry());
            obj.put("symbol", Currency.getInstance(defaultCode).getSymbol(local));
        } catch (JSONException ex) {
            Logger.log4j.error(ex);
        }
        return obj;
    }
    public static JSONObject _getCurrencyByCode(String code){
        Locale[] languages = Locale.getAvailableLocales();
        Locale local=null;
        for(Locale tempLocal : languages){
            try{
                String tempCode = Currency.getInstance(tempLocal).getCurrencyCode();
                if(tempCode.equals(code)){
                    JSONObject defaultCurrency = _getDefaultCurrency();
                    String defaultCode = defaultCurrency.getString("code");
                    if(defaultCode.equals(code)){
                        return defaultCurrency;
                    }
                    local = tempLocal;
                    break;
                }
            }catch(Exception err){
            }
        }
        if(local == null){
            local = Locale.getDefault();
        }
        String newCode = Currency.getInstance(local).getCurrencyCode();
        JSONObject obj = new JSONObject();
        try {
            obj.put("default", true);
            obj.put("code", newCode);
            obj.put("country", local.getDisplayCountry());
            obj.put("symbol", Currency.getInstance(newCode).getSymbol(local));
        } catch (JSONException ex) {
            Logger.log4j.error(ex);
        }
        return obj;
    }
    public static JSONArray _getCurrencyList(){
        Locale[] languages = Locale.getAvailableLocales();
        JSONArray arr = new JSONArray();
        Map<String,Integer> codeList = new HashMap<String, Integer>();
        int index = 0;
        String defaultCode = "";
        try {
            defaultCode = _getDefaultCurrency().getString("code");
        } catch (JSONException ex) {
            Logger.log4j.error(ex);
        }
        for(Locale local : languages){
            try{
                String code = Currency.getInstance(local).getCurrencyCode();
                if(codeList.containsKey(code)){
                    continue;
                }else{
                    codeList.put(code, index);
                }
                JSONObject obj = new JSONObject();             
                obj.put("code", code);
                if(defaultCode.equals(code)){
                    local = Locale.getDefault();
                    obj.put("default", true);
                }else{
                    obj.put("default", false);
                }
                String country = local.getDisplayCountry();
                obj.put("country", country);
                String symbol = Currency.getInstance(code).getSymbol(local);
                obj.put("symbol", symbol);
                //Logger.log4j.info(obj);
                arr.put(index++, obj);
            }catch(Exception err){
            }
        }
        return arr;
    }
    private static Map<String,String> INPUT_TYPE_UICLASS_MAP = new HashMap<String, String>(){{
        put("DECIMAL", "decimalTextBox");
        put("WHOLE_NUMBER", "numberTextBox");
        put("EMAIL", "emailTextBox");
        put("DATE", "dateTextBox");
        put("PHONE_NO", "contactNumberTextBox");
    }};
    private static Map<String,String> INPUT_TYPE_UITYPE_MAP = new HashMap<String, String>(){{
        put("DECIMAL", "text");
        put("WHOLE_NUMBER", "text");
        put("EMAIL", "email");
        put("DATE", "text");
        put("PHONE_NO", "text");
        put("PASSWORD", "password");
    }};
    public static JSONObject _getInputTypeByServerType(String formatType){
        JSONObject obj = new JSONObject();
        try{
            String inputType = INPUT_TYPE_UITYPE_MAP.containsKey(formatType)?
                    INPUT_TYPE_UITYPE_MAP.get(formatType):"text";
            obj.put("inputType", inputType);
            String inputClass = INPUT_TYPE_UICLASS_MAP.containsKey(formatType)?
                    INPUT_TYPE_UICLASS_MAP.get(formatType):"";
            obj.put("inputClass", inputClass);
        }catch(JSONException ex){
            Logger.log4j.error(ex);
        }
        return obj;
    }
    public static JSONObject _getCalcSize(long sizeInByte) throws JSONException{
        JSONObject obj = new JSONObject();
        int l = 1024;
        long kbLimit = (long) Math.pow(l, 1);
        long mbLimit = (long) Math.pow(l, 2);
        long gbLimit = (long) Math.pow(l, 3);
        double val = 0;
        String text = "SIZE_BYTES";
        if(sizeInByte >= gbLimit){
            val = (double)sizeInByte/gbLimit;
            text = "SIZE_GB";
        }else if(sizeInByte >= mbLimit){
            val = (double)sizeInByte/mbLimit;
            text = "SIZE_MB";
        }else if(sizeInByte >= kbLimit){
            val = (double)sizeInByte/kbLimit;
            text = "SIZE_KB";
        }else{
            val = sizeInByte;
            text = "SIZE_BYTES";
        }
        double afterDecimal = val - Math.floor(val);
        if(afterDecimal>0){
            obj.put("value", roundDecimals(val));
        }else{
            obj.put("value", (long)val);
        }
        text = play.i18n.Messages.get(text);
        obj.put("text", text);
        return obj;
    }
}
