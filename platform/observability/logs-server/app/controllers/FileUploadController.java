package controllers;

import actors.ProcessLogs;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import constants.ConstantGlobal;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;
import pojos.result.FileUploadRes;
import response.JsonResponse;
import utils.CommonUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Handles file upload
 * Created by Raghu Teja on 27-06-2017.
 */
public class FileUploadController extends Controller {

    private final Config config;
    private final ActorRef helloActor;

    @Inject public FileUploadController(ActorSystem system, Config config) {
        this.config = config;
        helloActor = system.actorOf(ProcessLogs.getProps(config));
    }

    private static final Logger.ALogger LOGGER = Logger.of("FileUploadController");

    public Result upload() {
        LOGGER.debug("Request arrived");
        MultipartFormData<File> body = request().body().asMultipartFormData();

        if(body == null) {
            String errorMessage = "No file or data has been uploaded";
            LOGGER.error(errorMessage);
            return badRequest(Json.toJson(new JsonResponse(null, errorMessage, "MISSING_FILE_OR_DATA")));
        }

        MultipartFormData.FilePart<File> logFile = body.getFile("uploaded_file");
        Map<String, String[]> data = body.asFormUrlEncoded();
        if (data != null) {
            for(Map.Entry<String, String[]> entry : data.entrySet()) {
                LOGGER.debug(entry.getKey() + ":   " + Arrays.toString(entry.getValue()));
            }
        }

        File logsDir = CommonUtils.getLogsDir(config, LOGGER);
        if(logsDir == null) {
            LOGGER.error("Unable to create required directories, check if name is valid");
            return internalServerError(Json.toJson(new JsonResponse(null,
                    "Please try later", "SERVICE_ERROR")));
        }

        if (logFile != null) {
            LOGGER.info("Valid, starting indexing");
            return sendForProcess(logFile, logsDir);
        } else {
            String errorMessage = "File is missing";
            LOGGER.error(errorMessage);
            return ok(Json.toJson(new JsonResponse(new FileUploadRes(), errorMessage,
                    "MISSING_FILE")));
        }
    }

    private Result sendForProcess(MultipartFormData.FilePart<File> logFile, File logsDir) {
        String fileName = logFile.getFilename();
        String contentType = logFile.getContentType();
        File file = logFile.getFile();
        LOGGER.debug("Filename: " + fileName);
        LOGGER.debug("ContentType: " + contentType);
        File newFile = new File(logsDir, fileName);
        boolean renamed = file.renameTo(newFile);
        LOGGER.debug("Renamed?: " + renamed);
        LOGGER.debug("File uploaded: " + newFile.getAbsolutePath());
        helloActor.tell(newFile, null);
        return ok(Json.toJson(new JsonResponse(new FileUploadRes(fileName))));
    }
}
