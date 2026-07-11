package com.lms.common.vedantu.entity.storage;

import java.util.HashMap;
import java.util.Map;

public enum MediaType {
    IMAGE("img", "jpg"),
    DOC("doc", "pdf"),
    VIDEO("vid", "mp4"),
    COMPRESSED("com", "zip"),
    ANIMATION("anime", "swf"),
    FILE("file", "txt"),
    AUDIO("aud","mp3"),
    UNKNOWN("", "");

    private static Map<String, MediaType> mapAcronym = null;

    private final String                  acronym;
    private final String                  defaultFileType;

    private MediaType(final String acronym, final String defaultFileType) {

        this.acronym = acronym;
        this.defaultFileType = defaultFileType;
    }

    public static MediaType valueOfKey(String type) {

        MediaType mediaType = MediaType.UNKNOWN;
        try {
            mediaType = MediaType.valueOf(type.toUpperCase());
        } catch (Exception e) {}
        return mediaType;
    }

    public String getAcronym() {

        return acronym;
    }

    public String getDefaultFileType() {

        return defaultFileType;
    }

    public static MediaType getByAcronym(String acronym) {

        acronym = (acronym != null) ? acronym.toLowerCase() : acronym;
        if (null == mapAcronym) {
            synchronized (MediaType.class) {
                if (null == mapAcronym) {
                    mapAcronym = new HashMap<String, MediaType>();
                    for (MediaType mediaType : MediaType.values()) {
                        mapAcronym.put(mediaType.acronym, mediaType);
                    }
                }
            }
        }
        return null != acronym && null != mapAcronym ? mapAcronym.get(acronym) : null;
    }

    public static void main(String[] args) {

        System.out.println("=================================");
        System.out.println(MediaType.getByAcronym("img"));
        System.out.println(MediaType.getByAcronym("vid"));
        System.out.println(MediaType.getByAcronym(null));
        System.out.println(MediaType.getByAcronym(MediaType.DOC.name()));
        System.out.println("=================================");
    }
}
