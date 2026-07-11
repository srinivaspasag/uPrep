package com.vedantu.ext.cmds.utils.config;

import java.util.Properties;

import com.vedantu.ext.cmds.utils.commons.StringUtils;

public class ErrorMessageUtils {

    static Properties prop;
    static {
        loadConfigs();
    }

    public static synchronized void loadConfigs() {

        prop = new Properties();
        try {
            prop.load(Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("errorcodes.properties"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getErrorMessage(String errorCode) {

        String errorMessage = prop.getProperty(errorCode);
        if (StringUtils.isEmpty(errorMessage)) {
            errorMessage = errorCode;
        }
        return errorMessage;
    }
}
