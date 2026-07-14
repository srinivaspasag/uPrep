package com.vedantu.ext.cmds.web;

import java.io.File;

import com.vedantu.ext.cmds.utils.config.Config;

public enum ReqAction {

    GET_ORG_INFO {

        @Override
        public String getUrl() {

            return getAbsoluteUrl("getOrgInfo");
        }
    },

    AUTHENTICATE {

        @Override
        public String getUrl() {

            return getAbsoluteUrl("authenticate");
        }
    },
    VALIDATE_ORG_APP_CREDENTIALS {

        @Override
        public String getUrl() {

            return getAbsoluteUrl("validateOrgAppCredentials");
        }
    },
    CREATE_FOLDER {

        @Override
        public String getUrl() {

            return getAbsoluteUrl("createFolder");
        }
    },
    GET_FOLDERS {

        @Override
        public String getUrl() {

            return getAbsoluteUrl("getFolders");
        }
    },
    GET_REMOVED_CONTENT_LINKS {

        @Override
        public String getUrl() {

            return getAbsoluteUrl("getRemovedContentLinks");
        }
    },
    GET_CONTENT_LINKS {

        @Override
        public String getUrl() {

            return getAbsoluteUrl("getContentLinks");
        }
    },

    GET_SD_CARD_GROUPS {

        @Override
        public String getUrl() {

            return getAbsoluteUrl("getSDCardGroups");
        }
    },
    GET_SD_CARD_GROUP_INFO {

        @Override
        public String getUrl() {

            return getAbsoluteUrl("getSDCardGroupInfo");
        }
    },
    GET_PROGRAMS {

        @Override
        public String getUrl() {

            return getAbsoluteUrl("getPrograms");
        }
    },
    GET_CENTERS_OF_PROGRAM {

        @Override
        public String getUrl() {

            return getAbsoluteUrl("getCentersOfProgram");
        }
    },
    GET_SECTIONS_OF_CENTER {

        @Override
        public String getUrl() {

            return getAbsoluteUrl("getSectionsOfCenter");
        }
    },

    GET_SECURE_URL {

        @Override
        public String getUrl() {

            return getAbsoluteUrl("getSecureURL");
        }
    },

    GET_LIBRARY_INFO {

        @Override
        public String getUrl() {

            return getAbsoluteUrl("getLibrary");
        }
    },
    
    GET_FILE_INFO {

        @Override
        public String getUrl() {

            return getAbsoluteUrl("getFileInfos");
        }
    },

    GET_CONTENTS {

        @Override
        public String getUrl() {

            return getAbsoluteUrl("getContents");
        }
    };

    private static String getAbsoluteUrl(String relativeUrl) {

        StringBuilder sb = new StringBuilder();
        sb.append(Config.REMOTE_HOST);
        sb.append(File.separator);
        sb.append(Config.REMOTE_ROUTER);
        sb.append(File.separator);
        sb.append(relativeUrl);
        return sb.toString();
    }

    public abstract String getUrl();
}
