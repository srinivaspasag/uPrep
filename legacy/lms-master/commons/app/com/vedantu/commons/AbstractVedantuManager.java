package com.vedantu.commons;

import play.Logger;
import play.Logger.ALogger;

/**
 * All Vedantu managers should extend {@link AbstractVedantuManager}. These managers should throw
 * only {@link VedantuException} if needed.
 * 
 * @author ujjawal
 * 
 */
// TODO unify with AbstractVedantuEventManager
public class AbstractVedantuManager {

    private static final ALogger LOGGER = Logger.of(AbstractVedantuManager.class);

}
