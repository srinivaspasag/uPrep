package com.vedantu.content.managers;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.billing.dao.OrderDAO;
import com.vedantu.commons.VedantuException;
import com.vedantu.content.daos.VirtualClassroomDAO;
import com.vedantu.content.models.VirtualClassroom;
import com.vedantu.content.pojos.requests.virtualclassroom.CreateRoomReq;
import com.vedantu.content.pojos.responses.virtualclassroom.CreateRoomRes;

import play.Logger;
import play.Logger.ALogger;

public class VirtualClassroomManager {

    private static final ALogger         LOGGER             = Logger.of(VirtualClassroomManager.class);
    public static final long             Duration           = 7200000;
    public static final String           LearnCubePublicKey = "8438affca1baa3691f04d71a";
    public static final SimpleDateFormat iso8601formattter  = new SimpleDateFormat(
                                                                    "yyyy-MM-dd'T'HH:mm:ss'Z'");
    public static final int              MaxParticipants    = 40;

    public static CreateRoomRes createClassroom(CreateRoomReq req) throws VedantuException {
        if (req.startTime == 0) {
            req.startTime = System.currentTimeMillis();
        }
        req.endTime = req.startTime + Duration;
        VirtualClassroom room = VirtualClassroomDAO.INSTANCE.createClassroom(req.description,
                req.startTime, req.endTime, req.recordClass, req.cancelled, req.audioOnly,
                req.userId, req.orgId);
        try {
            createClassroomInLearntube(room);
        } catch (JSONException e) {
            LOGGER.error("JSONException in createClassroom");
            e.printStackTrace();
        } catch (IOException e) {
            LOGGER.error("IOException in createClassroom");
            e.printStackTrace();
        }
        CreateRoomRes res = new CreateRoomRes();
        res.success = true;
        res.publicKey = LearnCubePublicKey;
        res.roomToken = room._getStringId();
        return res;
    }

    private static void createClassroomInLearntube(VirtualClassroom room) throws JSONException, IOException {
        String url = "https://app.learncube.com/api/virtual-classroom/classrooms/";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // Setting basic post request
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("api_public_key", LearnCubePublicKey);

        JSONObject payload = new JSONObject();
        payload.put("room_token", room._getStringId());
        payload.put("cancelled", room.cancelled);
        payload.put("description", room.description);
        payload.put("start", getISO8601Time(room.startTime));
        payload.put("end", getISO8601Time(room.endTime));
        payload.put("max_participants", MaxParticipants);
        payload.put("audio_only", room.audioOnly);
        payload.put("return_url", "https://learn.learnpedia.in/home");
        payload.put("record_class", room.recordClass);

        String postJsonData = payload.toString();

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(postJsonData);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        LOGGER.debug("nSending 'POST' request to URL : " + url);
        LOGGER.debug("Post Data : " + postJsonData);
        LOGGER.debug("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String output;
        StringBuffer response = new StringBuffer();

        while ((output = in.readLine()) != null) {
            response.append(output);
        }
        in.close();
        LOGGER.debug("response : " + response.toString());
        JSONObject payloadResp = new JSONObject(response.toString());
        if (payloadResp.length() > 0) {
            room.uuid = payloadResp.getString("uuid");
            VirtualClassroomDAO.INSTANCE.save(room);
        }
    }

    public static String getISO8601Time(long startTime) {
        return iso8601formattter.format(startTime);
    }
}
