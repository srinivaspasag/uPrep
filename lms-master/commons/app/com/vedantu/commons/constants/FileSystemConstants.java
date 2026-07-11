package com.vedantu.commons.constants;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

public class FileSystemConstants {
	private static final ALogger	LOGGER											= Logger.of(FileSystemConstants.class);
	public static final String		TEMP_DIR										= Play.application()
																							.configuration()
																							.getString(
																									"util.temp_dir");

	@SuppressWarnings("unused")
	private static String			IMG_HOST										= "http://localhost:9123";
	private static final String		IMG_HOST_PREFIX									= "http://img";
	public static String			DEFAULT_HOST_URL_STATUS_FEED_EMBED_TEMP_IMAGES	= "/temp/statusfeed/";
	static {
		try {
			String host = Play.application().configuration()
					.getString("app.host");
			IMG_HOST = IMG_HOST_PREFIX + "." + host;

		} catch (Exception e) {
			LOGGER.error("can not load property app.host for application run mode : "
					+ Play.application().configuration()
							.getString("application.mode"));
		}

	}

}
