package com.vedantu.viewers.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.fs.handlers.LocalFileSystemHandler;

public class DefaultPicUtil {

	private static final ALogger LOGGER = Logger.of(DefaultPicUtil.class);

	public static InputStream get(EntityType type) throws FileNotFoundException {
		// TODO check based on type to serve different default files

		File defaultFile = Play.application().getFile(
				LocalFileSystemHandler.PATH_SEPARATOR
						+ "public"
						+ LocalFileSystemHandler.PATH_SEPARATOR
						+ "images"
						+ LocalFileSystemHandler.PATH_SEPARATOR
						+ Play.application().configuration()
								.getString("default.user.profile.pic"));
		LOGGER.debug(" Looking for default pic at "
				+ defaultFile.getAbsolutePath());
		return new FileInputStream(defaultFile);

	}
}
