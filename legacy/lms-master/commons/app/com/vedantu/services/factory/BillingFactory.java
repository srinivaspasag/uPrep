package com.vedantu.services.factory;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

public class BillingFactory {

    private static final ALogger             LOGGER        = Logger.of(BillingFactory.class);

    private static final Map<String, String> serviceUrlMap = new HashMap<String, String>();

    public static final BillingFactory       INSTANCE      = new BillingFactory();

    private BillingFactory() {

        serviceUrlMap.put(StringUtils.lowerCase("createInvoice"), "localhost:19022");

    }

    public String getServiceUrl(String funtionName) {

        String serviceUrl = serviceUrlMap.get(StringUtils.lowerCase(funtionName));
        LOGGER.info("service url for functionName[" + funtionName + "] : " + serviceUrl);
        return serviceUrl;
    }
}
