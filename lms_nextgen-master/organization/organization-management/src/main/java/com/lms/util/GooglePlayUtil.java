package com.lms.util;

import com.lms.common.utils.FileUtils;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

@Setter
@Getter
public class GooglePlayUtil {
    @Value("${google.playstore.base.url}")
    private String baseUrl;
    private static final Logger logger = LoggerFactory.getLogger(GooglePlayUtil.class);

    public static String APP_STORE="GOOGLE";
    public  String getUrlForOrganizationApp(String slug) {

        // before this check if app is feature for organization
        String baseURL = "https://play.google.com/store/apps/details?id=";
        String orgAppURL = baseURL + FileUtils.SEPARATOR_DOT + slug;

        return orgAppURL;
    }
}
