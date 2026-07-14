package com.vedantu.organization.pojos.utils;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.vedantu.commons.utils.FileUtils;

public class GooglePlayUtil {

    private static final ALogger LOGGER = Logger.of(GooglePlayUtil.class);

    public static String APP_STORE="GOOGLE";
    public static String getUrlForOrganizationApp(String slug) {

        // before this check if app is feature for organization
        String baseURL = Play.application().configuration().getString("google.playstore.base.url");
        // https://play.google.com/store/apps/details?id=
        String orgAppURL = baseURL + FileUtils.SEPARATOR_DOT + slug;

        return orgAppURL;
    }
}
