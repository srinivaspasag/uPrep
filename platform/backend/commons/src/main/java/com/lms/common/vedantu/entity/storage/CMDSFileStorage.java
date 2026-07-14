package com.lms.common.vedantu.entity.storage;

import com.lms.common.vedantu.enums.ImageSize;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Map;

@Component
public class CMDSFileStorage extends AbstractEntityFileStorage {

	/*
	 * public CMDSFileStorage() { super(EntityType.CMDSFILE); }
	 */

	@Override
	public StorageResult storeImage(final String uid, final File file, final FileCategory fileCategory,
									final ImageSize imageSize, final Map<String, String> tags) throws EntityFileStorageException {
		return super.storeImage(uid, file, fileCategory, imageSize, tags);
	}

	@Override
	public StorageResult storeVideo(final String uid, final File file, final FileCategory fileCategory,
									final Map<String, String> tags, MediaType mediaType) throws EntityFileStorageException {
		return super.storeVideo(uid, file, fileCategory, tags, mediaType);
	}

}
