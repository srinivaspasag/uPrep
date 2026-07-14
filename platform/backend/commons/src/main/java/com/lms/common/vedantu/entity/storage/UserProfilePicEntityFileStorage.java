package com.lms.common.vedantu.entity.storage;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.commons.pojos.requests.FileData;
import com.lms.common.vedantu.enums.ImageSize;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Map;

@Component
public class UserProfilePicEntityFileStorage extends AbstractEntityFileStorage {
	@Override
	public StorageResult storeImage(final String uid, final File file,
									final FileCategory fileCategory, final ImageSize imageSize,
									final Map<String, String> tags) throws VedantuException {
		return super.storeImage(uid, file, fileCategory, imageSize, tags);
	}

	@Override
	public FileData getData(String entityType, String mediaType, String fileName) throws VedantuException {
		return super.getData(entityType, mediaType, fileName);
	}
}
