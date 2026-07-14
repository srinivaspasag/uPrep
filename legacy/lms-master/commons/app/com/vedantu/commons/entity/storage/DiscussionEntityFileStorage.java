package com.vedantu.commons.entity.storage;

import java.io.File;
import java.util.Map;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.FileData;

public class DiscussionEntityFileStorage extends AbstractEntityFileStorage {

	public DiscussionEntityFileStorage() {
		super(EntityType.DISCUSSION);
	}

	@Override
	public StorageResult storeImage(final String uid, final File file,
			final FileCategory fileCategory, final ImageSize imageSize,
			final Map<String, String> tags) throws EntityFileStorageException {
		return super.storeImage(uid, file, fileCategory, imageSize, tags);
	}

	@Override
	public FileData getData(String entityType, String mediaType, String fileName)
			throws EntityFileStorageException {
		return super.getData(entityType, mediaType, fileName);
	}
}
