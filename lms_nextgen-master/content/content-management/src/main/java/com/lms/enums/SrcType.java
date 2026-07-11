package com.lms.enums;

public enum SrcType {
    WEB_PAGE, LINK_VIDEO, VIDEO, IMAGE, TEXT, VEDANTU, DOCUMENT, QUESTION, TEST, UNKNOWN;

    public static SrcType valueOfKey(String value) {
        SrcType srcType = UNKNOWN;
        try {
            srcType = valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
        }
        return srcType;
    }

    public static enum LinkType {
        ADDED, UPLOADED, VEDANTU;
        public static LinkType valueOfKey(String value) {
            LinkType linkType = VEDANTU;
            try {
                linkType = valueOf(value.trim().toUpperCase());
            } catch (Exception e) {
            }
            return linkType;
        }
    }
}
