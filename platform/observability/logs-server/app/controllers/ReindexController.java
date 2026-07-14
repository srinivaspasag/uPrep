package controllers;

import actors.ProcessLogs;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.util.Timeout;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import pojos.result.ReindexRes;
import response.JsonResponse;
import scala.compat.java8.FutureConverters;
import scala.concurrent.Future;
import utils.CommonUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static akka.pattern.Patterns.ask;

/**
 * Created by Raghu Teja on 28-07-2017.
 */
public class ReindexController extends Controller {

    private final Config config;
    private final ActorRef helloActor;

    private static final Logger.ALogger LOGGER = Logger.of("ReindexController");

    private final LogFileFilter LOG_FILTER;

    @Inject
    public ReindexController(ActorSystem system, Config config) {
        this.config = config;
        helloActor = system.actorOf(ProcessLogs.getProps(config));
        LOG_FILTER = new LogFileFilter();
    }

    public Result reindex() {
        File logsDir = CommonUtils.getLogsDir(config, LOGGER);
        if(logsDir == null) {
            LOGGER.error("Unable to create required directories, check if name is valid");
            return internalServerError(Json.toJson(new JsonResponse(null,
                    "Please try later", "SERVICE_ERROR")));
        }
        File[] logFiles = logsDir.listFiles(LOG_FILTER);
        if(logFiles == null || logFiles.length == 0) {
            LOGGER.debug("No files remaining for reindexing");
            return ok(Json.toJson(new JsonResponse(new ReindexRes("No files remaining to reindex in " + logsDir.getAbsolutePath()))));
        }
        LOGGER.debug("Found " + logFiles.length + " files for reindexing");
        for(File logFile:logFiles) {
            if(logFile.isDirectory() || !logFile.exists()) {
                continue;
            }
            LOGGER.debug("Indexing: " + logFile.getName());
            helloActor.tell(logFile, null);
        }
        return ok(Json.toJson(new JsonResponse(new ReindexRes("Reindexing started"))));
    }

    public CompletionStage<Result> checkReindex() {
        return FutureConverters.toJava(ask(helloActor, "status", new Timeout(10L, TimeUnit.SECONDS)))
                .thenApply(response -> {
                    String resp;
                    if (response instanceof String) {
                        resp = (String) response;
                    }
                    else {
                        resp = "Actor is dead or busy";
                    }

                    if(!resp.equals("GOOD")) {
                        return internalServerError(resp);
                    }
                    return ok(resp);
                });
    }

    private class LogFileFilter implements FilenameFilter {
        private final Pattern logPattern;

        LogFileFilter() {
            logPattern = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{6}\\.zip");
        }

        @Override
        public boolean accept(File dir, String name) {
            LOGGER.debug("Checking for " + name);
            return logPattern.matcher(name).matches();
        }
    }
}
