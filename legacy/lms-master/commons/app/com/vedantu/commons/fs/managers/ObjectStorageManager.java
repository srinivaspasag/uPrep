package com.vedantu.commons.fs.managers;

import java.io.IOException;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.vedantu.commons.fs.objectstorage.Container;
import com.vedantu.commons.fs.objectstorage.ObjectFile;
import com.vedantu.commons.fs.objectstorage.ObjectFileEx;

public class ObjectStorageManager {

    private static ALogger              LOGGER   = Logger.of(ObjectStorageManager.class);

    private static ObjectStorageManager instance = null;
    public String                       username = null;
    public String                       password = null;
    public String                       baseUrl  = null;

    private ObjectStorageManager() {

        username = Play.application().configuration().getString("objectstorage.username");
        password = Play.application().configuration().getString("objectstorage.password");
        baseUrl = Play.application().configuration().getString("objectstorage.baseurl");

    }

    public static ObjectStorageManager get() {

        if (instance == null) {
            synchronized (ObjectStorageManager.class) {
                if (instance == null) {
                    instance = new ObjectStorageManager();
                }

            }
        }
        return instance;
    }

    public String getName() {

        return username;
    }

    public String getPassword() {

        return password;
    }

    public String getBaseUrl() {

        return baseUrl;
    }

    public ObjectFile getObjectFile(String containerName, String fileName) throws IOException {

        return new ObjectFile(fileName, containerName, baseUrl, username, password, true);
    }

    public Container getContainer(String containerName) throws IOException {

        LOGGER.debug("OS userName " + username);
        return new Container(containerName, baseUrl, username, password, true);

    }

    public ObjectFileEx getObjectFileExtended(String containerName, String fileName)
            throws IOException {

        return new ObjectFileEx(fileName, containerName, baseUrl, username, password, true);
    }

}
