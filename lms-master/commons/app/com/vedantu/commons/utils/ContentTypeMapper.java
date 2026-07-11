package com.vedantu.commons.utils;

import java.io.IOException;

import javax.activation.MimetypesFileTypeMap;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.vedantu.commons.constants.Configurations;
/**
 * This class is a mapper for content type and extension of the file type being
 * uploaded
 * 
 * @author
 * 
 */
public class ContentTypeMapper {

	private final static ALogger LOGGER = Logger.of(ContentTypeMapper.class);
	public static final String CONTENT_TYPE = "ContentType";
	private MimetypesFileTypeMap typeMap = null;
	private static ContentTypeMapper instance = null;

	private ContentTypeMapper() throws IOException {

		Logger.info("Reading mime type configuration file from "
				+ Play.application().path()
				+ "/"
				+ Play.application().configuration()
						.getString(Configurations.MIME_TYPES_FILE_PATH));
		typeMap = new MimetypesFileTypeMap(Play.application().path()
				+ "/"
				+ Play.application().configuration()
						.getString(Configurations.MIME_TYPES_FILE_PATH));
	}

	public static ContentTypeMapper get() {
		if (instance == null) {
			synchronized (ContentTypeMapper.class) {
				if (instance == null) {
					try {
						instance = new ContentTypeMapper();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						LOGGER.error("Please verify specified path for mime type file :"
								+ Play.application().path()
								+ "/"
								+ Play.application().configuration()
										.getString(Configurations.MIME_TYPES_FILE_PATH), e);
					}
				}

			}
		}
		return instance;
	}

	public String getContentType(String fileName) {
		return typeMap.getContentType(fileName);
	}

	public String getContentTypeFromExtension(String extension) {

		return typeMap.getContentType("dummy." + extension);
	}

	public static void main(String[] args) {

		System.out.println(ContentTypeMapper.get().getContentType("1.pdf"));
	}

}
