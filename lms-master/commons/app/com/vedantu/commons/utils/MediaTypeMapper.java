package com.vedantu.commons.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.vedantu.commons.entity.storage.MediaType;

public class MediaTypeMapper {

    private final static ALogger   LOGGER = Logger.of(MediaTypeMapper.class);
    private Map<String, MediaType> mediaTypeMap;
    private String                 mediaTypeConfFileName;
    private static MediaTypeMapper instance;

    private MediaTypeMapper() throws Exception {

        mediaTypeConfFileName = Play.application().configuration()
                .getString("media.types.file.path");
        mediaTypeMap = new HashMap<String, MediaType>();
        LOGGER.info("play app path: " + Play.application().path());
        FileReader fr;
        fr = new FileReader(new File(Play.application().path() + "/" + mediaTypeConfFileName));
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null) {
            String[] keyValue = StringUtils.split(line, ' ');
            String key = keyValue[0];
            MediaType value = MediaType.valueOfKey(keyValue[1]);
            mediaTypeMap.put(key, value);
        }
        br.close();
    }

    public static MediaTypeMapper INSTANCE() {

        if (instance == null) {
            synchronized (MediaTypeMapper.class) {
                if (instance == null) {
                    try {
                        instance = new MediaTypeMapper();
                    } catch (IOException e) {
                        LOGGER.error("Please verify specified path for media type file :"
                                + Play.application()
                                        .configuration()
                                        .getString(
                                                Play.application().path() + "/"
                                                        + "media.types.file.path"));
                    } catch (Exception e) {
                        LOGGER.error("Please verify specified path for media type file :"
                                + Play.application()
                                        .configuration()
                                        .getString(
                                                Play.application().path() + "/"
                                                        + "media.types.file.path"));
                    }
                }

            }
        }
        return instance;
    }

    public MediaType getMediaType(String fileExtension) {

        if (!this.mediaTypeMap.containsKey(fileExtension)) {
            return MediaType.UNKNOWN;
        }
        return this.mediaTypeMap.get(fileExtension);
    }
}
