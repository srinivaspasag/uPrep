package com.virtualClassRoomService.virtualClassRoomService.Helpers;

import com.virtualClassRoomService.virtualClassRoomService.Pojos.Requests.CreateMeetingRoomRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Random;
import java.util.UUID;

@Component
public class URLCreationHelper {

    private static Logger log = LoggerFactory.getLogger(URLCreationHelper.class);

    @Value("${secret-key}")
    private String secretKey;
    @Value(("${base-url}"))
    private String baseUrl;


    public String createMeeting1(CreateMeetingRoomRequest request) throws IOException {

        URL url = new URL("https://testvc.uprep.in/bigbluebutton/api/create?name=Test&meetingID=f95c453a-60d0-4a84-8dd9-e0dbadf07a44&attendeePW=uprep123&moderatorPW=learn123&checksum=4498b26da58458574bd54ce5feb345dbd8f86047");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.connect();
        String response=getConnectionResult(connection,false);
        int responseCode=connection.getResponseCode();
        log.info("response : "+response);
        return response;
    }
    public static String getConnectionResult(HttpURLConnection connection, boolean useErrorStream) throws IOException {
        BufferedReader reader;
        String newLine = System.getProperty("line.separator", "\r\n");
        if (!useErrorStream) {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        }
        String line;
        StringBuilder result = new StringBuilder();
        while((line = reader.readLine()) != null) {
            result.append(line);
            result.append(newLine);
        }
        reader.close();
        return result.toString();
    }
//    public CreateMeetingResponse createMeeting(CreateMeetingRoomRequest request) {
//        String url = "https://testvc.uprep.in/bigbluebutton/api/create?name=Test&meetingID=f95c453a-60d0-4a84-8dd9-e0dbadf07a44&attendeePW=uprep123&moderatorPW=learn123&checksum=4498b26da58458574bd54ce5feb345dbd8f86047";
//        RestTemplate restTemplate = new RestTemplate();
//        CreateMeetingResponse response = restTemplate.getForObject(url, CreateMeetingResponse.class);
//        Meeting meeting=new Meeting(response.getMeetingID(),response.getAttendeePW(),response.getModeratorPW(),
//                response.getInternalMeetingID(),request.className,request.subject,request.topic,
//                response.getCreateTime(),response.getDuration(),request.sectionId,request.orgId,
//                request.userId,request.createdTime);
//        meetingRepository.save(meeting);
//        return response;
//    }

    public String createUrl(String className) throws IOException {
        String url = baseUrl;
        String attendeePW=generateRandomString();
        String moderatorPW =generateRandomString();
        UUID uuid = UUID.randomUUID();
        String querParams="name="+className+"&meetingID="+uuid+"&attendeePW="+attendeePW+"&moderatorPW="+moderatorPW;
        String keyforchecksum="create"+querParams+secretKey;
        String checksumvalue=generateHash(keyforchecksum);
        querParams="create?"+querParams+"&checksum="+checksumvalue;
        url+=querParams;
        return url;
    }
    public String generateRandomString(){
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();
        String randomString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return randomString;
    }
    public String generateHash(String keyforchecksum){
        String checksumvalue="";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            digest.update(keyforchecksum.getBytes("utf8"));
            checksumvalue = String.format("%040x", new BigInteger(1, digest.digest()));
        } catch (Exception e){
            e.printStackTrace();
        }
        return checksumvalue;
    }


}
