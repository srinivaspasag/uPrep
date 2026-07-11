package utils;

import com.typesafe.config.Config;
import constants.ConstantGlobal;
import play.Logger;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * Created by Raghu Teja on 28-07-2017.
 */
public class CommonUtils {

    public static File getLogsDir(Config config, Logger.ALogger LOGGER) {
        String logPath = config.getString(ConstantGlobal.LOGS_PATH);
        LOGGER.debug("LogPath: " + logPath);
        File logsDir = new File(logPath);
        if(!logsDir.exists()) {
            boolean dirsCreated = logsDir.mkdirs();
            if(!dirsCreated) {
                LOGGER.error("Directories not created", new IOException("Unable to create directories"));
                return null;
            }
        }
        return logsDir;
    }

    public static void close(Closeable... closeables) {
        if(closeables == null) {
            return;
        }

        for(Closeable closeable : closeables) {
            if(closeable == null) {
                continue;
            }
            try {
                closeable.close();
            } catch (IOException ignored) {}
        }
    }
}
