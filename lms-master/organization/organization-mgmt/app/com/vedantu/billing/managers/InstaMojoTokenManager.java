package com.vedantu.billing.managers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.models.Organization;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;
import com.vedantu.organization.pojos.responses.organizations.InstaMojoAccessTokenResp;

import play.Logger;
import play.Play;
import play.Logger.ALogger;

public class InstaMojoTokenManager {
	private static final ALogger LOGGER = Logger.of(InstaMojoTokenManager.class);
	private static final String  INSTAMOJOURL  = Play.application().configuration().getString("instamojo.url");
	private static final String  SECURE_API_URL = "http://apigateway.learnpedia.in/secure-api/index.php/access-token";
	private static final String  CLIENT_CREDENTIALS  = "client_credentials";

	public static InstaMojoAccessTokenResp getInstaMojoAccessToken(AbstractOrgScopeReq getOrganizationReq) {
		String  INSTAMOJOCLIENTID  = Play.application().configuration().getString("instamojo.clientid");
		String  INSTAMOJOCLIENTSECRETKEY  = Play.application().configuration().getString("instamojo.clientsecretkey");
		//GETTING ANIL NAIR'S CREDENTIALS for anc and skillmithra
		Organization org = OrganizationDAO.INSTANCE.getById(getOrganizationReq.orgId);
		if (org != null) {
			if (!StringUtils.isEmpty(org.instaMojoClientId)
					&& !StringUtils.isEmpty(org.instaMojoClientSecret)) {
				LOGGER.debug("using Client's credentials for orgId  :"
						+ getOrganizationReq.orgId);
				INSTAMOJOCLIENTID = org.instaMojoClientId;
				INSTAMOJOCLIENTSECRETKEY = org.instaMojoClientSecret;
			}
		}
		InstaMojoAccessTokenResp resp = new InstaMojoAccessTokenResp();
		URL url;
		String instaMojoResponse = "";
		String postData = "";
		try {
			url = new URL(INSTAMOJOURL);
			LOGGER.debug("INSTAMOJOCLIENTID" + INSTAMOJOCLIENTID);
			LOGGER.debug("INSTAMOJOCLIENTSECRETKEY" + INSTAMOJOCLIENTSECRETKEY);
			postData += "grant_type="+CLIENT_CREDENTIALS+"&client_id="+INSTAMOJOCLIENTID+"&client_secret="+INSTAMOJOCLIENTSECRETKEY;
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("cache-control", "no-cache");
			connection.addRequestProperty("content-type","application/x-www-form-urlencoded");
			connection.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write(postData);
            out.close();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String decodedString;
            while ((decodedString = in.readLine()) != null) {
                instaMojoResponse += decodedString;
            }
            in.close();
		} catch (MalformedURLException e1) {
			LOGGER.info("exception malformed " + e1.getMessage());
		} catch (IOException e) {
			LOGGER.info("exception IOException " + e.getMessage());
		}
		LOGGER.info("json data " + instaMojoResponse.toString());
		JSONObject obj = null;
		try {
			obj = new JSONObject(instaMojoResponse);
			if(obj.has("access_token")){
					resp.access_token = Play.application().configuration().getString("instamojo.env")+obj.getString("access_token");
			}
		} catch (JSONException e) {
			LOGGER.debug("ERROR IN GETTING ACCESS TOKEN    " + e.getMessage());
		}
		return resp;
	}

	public static InstaMojoAccessTokenResp getInstaMojoAccessTokenFromPHP(AbstractOrgScopeReq getOrganizationReq) {
        String  INSTAMOJOCLIENTID  = Play.application().configuration().getString("instamojo.clientid");
        String  INSTAMOJOCLIENTSECRETKEY  = Play.application().configuration().getString("instamojo.clientsecretkey");
        //GETTING ANIL NAIR'S CREDENTIALS for anc and skillmithra
        Organization org = OrganizationDAO.INSTANCE.getById(getOrganizationReq.orgId);
        if (org != null) {
            if (!StringUtils.isEmpty(org.instaMojoClientId)
                    && !StringUtils.isEmpty(org.instaMojoClientSecret)) {
                LOGGER.debug("using Client's credentials for orgId  :"
                        + getOrganizationReq.orgId);
                INSTAMOJOCLIENTID = org.instaMojoClientId;
                INSTAMOJOCLIENTSECRETKEY = org.instaMojoClientSecret;
            }
        }
        InstaMojoAccessTokenResp resp = new InstaMojoAccessTokenResp();
        URL url;
        String instaMojoResponse = "";
        String postData = "";
        try {
            url = new URL(SECURE_API_URL);
            LOGGER.debug("INSTAMOJOCLIENTID" + INSTAMOJOCLIENTID);
            LOGGER.debug("INSTAMOJOCLIENTSECRETKEY" + INSTAMOJOCLIENTSECRETKEY);
            postData += "grant_type="+CLIENT_CREDENTIALS+"&client_id="+INSTAMOJOCLIENTID+"&client_secret="+INSTAMOJOCLIENTSECRETKEY+"&instamojo_url="+INSTAMOJOURL;
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("cache-control", "no-cache");
            connection.addRequestProperty("content-type","application/x-www-form-urlencoded");
            connection.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write(postData);
            out.close();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String decodedString;
            while ((decodedString = in.readLine()) != null) {
                instaMojoResponse += decodedString;
            }
            in.close();
        } catch (MalformedURLException e1) {
            LOGGER.info("exception malformed " + e1.getMessage());
        } catch (IOException e) {
            LOGGER.info("exception IOException " + e.getMessage());
        }
        LOGGER.info("json data " + instaMojoResponse.toString());
        JSONObject obj = null;
        try {
            obj = new JSONObject(instaMojoResponse);
            if(obj.has("access_token")){
                    resp.access_token = Play.application().configuration().getString("instamojo.env")+obj.getString("access_token");
            }
        } catch (JSONException e) {
            LOGGER.debug("ERROR IN GETTING ACCESS TOKEN    " + e.getMessage());
        }
        return resp;
    }
}
