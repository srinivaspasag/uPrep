package com.vedantu;

import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.Logger.ALogger;
import play.cache.Cache;

import com.vedantu.commons.utils.AppUtils;
import com.vedantu.mongo.MorphiaManager;

public class VedantuGlobalSettings extends GlobalSettings {

    private static final ALogger LOGGER = Logger.of(VedantuGlobalSettings.class);

    @Override
    public void beforeStart(Application app) {

        LOGGER.info("BEFORE START ........................");
        MorphiaManager.INSTANCE.__initailize(app);
    }

    @Override
    public void onStop(Application app) {

        Cache.remove(AppUtils.getAppKey());
        LOGGER.info("IN STOP........................");
    }

}
