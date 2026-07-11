package com.vedantu.ext.cmds.utils.config;

import java.util.Properties;

import com.vedantu.ext.cmds.db.datamanagers.OrgDataManager;
import com.vedantu.ext.cmds.db.models.Organization;

/**
 * 
 * This reads from remote.properties
 * 
 * @author vikram
 * 
 */
public class Config {

    public static String REMOTE_HOST         = "";
    public static String REMOTE_ROUTER       = "";
    public static String APP_ID              = "";
    public static String DESKTOP_FOLDER_NAME = "";
    public static String DESKTOP_LOCATION    = "";

    static {
        loadConfigs();
    }

    public static synchronized void loadConfigs() {

        Properties prop = new Properties();
        try {
            prop.load(Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("remote.properties"));
            Organization org = OrgDataManager.INSTANCE.getOrganization();
            if (org != null) {
                REMOTE_HOST = org.host;
            } else {
                REMOTE_HOST = prop.getProperty("remote.host.url");
            }

            REMOTE_ROUTER = prop.getProperty("remote.host.router");
            APP_ID = prop.getProperty("desktop.app.id");
            DESKTOP_FOLDER_NAME = prop.getProperty("desktop.app.folder.name");
            DESKTOP_LOCATION = prop.getProperty("desktop.storage.location");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
