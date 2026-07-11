package controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Play;
import play.mvc.Controller;
import play.mvc.With;
import uicom.util.ClientUtil;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.ning.http.multipart.FilePart;

@With(Security.class)
public class Upload extends Controller {
    private static final String pathToUpload = Play.getFile("util.temp_dir")
            .getAbsolutePath();

    public static void upload() {
        render();
    }

    public static void preview(JSONArray json) {
        Logger.log4j.info("json is " + json);
        render(json);
    }

    public static void uploadQuestions(String qqfile) {
        String UPLOAD_PATH = pathToUpload + File.separator + "uploads";
        Logger.log4j.info("starting the upload progress");
        if (request.isNew) {
            FileOutputStream moveTo = null;

            Logger.info("Name of the file %s", qqfile);
            // Another way I used to grab the name of the file
            String filename = request.headers.get("x-file-name").value();

            Logger.info("Absolute on where to send %s", UPLOAD_PATH
                    + File.separator);
            InputStream data = null;
            try {

                Logger.log4j
                        .info("reading input stream from request body header");
                data = request.body;
                Logger.log4j
                        .info("successfully read file data from request body header");
                File up = new File(UPLOAD_PATH);
                if (!up.exists()) {
                    Logger.log4j.info("making directory "
                            + up.getAbsolutePath() + ", result is : "
                            + up.mkdir());
                }
                File inputDoc = new File(UPLOAD_PATH + File.separator
                        + filename);
                moveTo = new FileOutputStream(inputDoc);
                Logger.log4j.info("cpoying file to questions's local system");
                IOUtils.copy(data, moveTo);
                Logger.log4j.info("file copied to local system : "
                        + inputDoc.getAbsolutePath());
                moveTo.close();
                data.close();
                AsyncHttpClient asynClient = new AsyncHttpClient();
                Logger.log4j.info("uploading file to Questions service");
                Future<Response> response = asynClient
                        .preparePost(
                                ClientUtil.CMDS_SERVICE_URL
                                        + "/cmdsQuestions/uploadQuestion?userId="
                                        + session.get("userId")
                                        + "&appId="+Play.configuration.getProperty("auth.appId")
                                        + "&secretKey="+Play.configuration.getProperty("auth.secretKey")
                                        +"&organizationId="+session.get("organizationId"))
                        .addHeader("Content-Type",
                                "multipart/form-data;boundary=randomBoundaryNotInAnyOfParts")
                        .addBodyPart(new FilePart("file", inputDoc))
                        .execute();
                Response r = null;

                JSONObject jsonResponse = new JSONObject();
                try {
                    r = response.get();
                    String rspString = r.getResponseBody();
                    jsonResponse = new JSONObject(rspString);
                    Logger.log4j
                            .info("uploaded doc got deleted from local dir : "
                                    + inputDoc.delete());
                    Logger.log4j.info("status" + jsonResponse);
                } catch (InterruptedException e) {
                    Logger.log4j.error(e.getMessage(), e);
                } catch (ExecutionException e) {
                    Logger.log4j.error(e.getMessage(), e);
                } catch (JSONException e) {
                    Logger.log4j.error(e.getMessage(), e);
                } finally {
                    asynClient.close();
                    if (data != null) {
                        Logger.log4j.info("closing data input stream");
                        data.close();
                    }
                }

            } catch (Exception ex) {
                Logger.log4j.error(ex.getMessage(), ex);
                renderJSON("{success: false}");
            }

        }
        renderJSON("{success: true}");
    }

    public static void uploadMetadataFile() {
        render();
    }
}