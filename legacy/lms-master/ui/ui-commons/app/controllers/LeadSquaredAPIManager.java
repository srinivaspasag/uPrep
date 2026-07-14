package controllers;

import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;


public class LeadSquaredAPIManager {

    public static final String LSQ_BASE_URL = "https://api.leadsquared.com/v2/";
    public static final String LSQ_CREATE_LEAD_URL = "LeadManagement.svc/Lead.Capture";
    private String accessKey;
    private String secretKey;

    public LeadSquaredAPIManager (String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public String addLead(String leadJSONString) throws Exception {
        try {
            String url = LSQ_BASE_URL + LSQ_CREATE_LEAD_URL + "?accessKey=" + this.accessKey + "&secretKey=" + this.secretKey;
            return postData (url, leadJSONString);
        } catch (Exception e){
                    throw e;
        }
    }

    public String postData(String url, String leadJsonString) throws Exception {
        try {
                    URL lsqURL = new URL (url);
                    HttpURLConnection connection = (HttpURLConnection) lsqURL.openConnection();
                    connection.setRequestMethod("POST");
                    connection.addRequestProperty ("Content-Type", "application/json");
                    connection.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                    wr.write(leadJsonString);
                    wr.flush();

                    InputStream in = connection.getInputStream();
                    BufferedReader res = new BufferedReader(new InputStreamReader(in, "UTF-8"));

                    StringBuffer sBuffer = new StringBuffer();
                    String inputLine;

                    while ((inputLine = res.readLine()) != null)
                        sBuffer.append(inputLine);

                    res.close();
                    return sBuffer.toString();
        }catch(Exception ex){
                      throw ex;
        }
    }
}
