package com.vedantu.commons.entity.storage;

import java.io.File;
import java.util.Map;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.FileData;

public class FileStorage extends AbstractEntityFileStorage {

	private static final ALogger	LOGGER	= Logger.of(FileStorage.class);

	public FileStorage() {
		super(EntityType.FILE);
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

//	 @Override
//	 protected boolean storeInFS(final File file, final String fileId,
//	 final Map<String, String> tags) throws EntityFileStorageException {
//	 try {
//	 return FileSystemFactory.INSTANCE.getLocalFS().store(
//	 file.getAbsolutePath(), folderId, fileId,
//	 (null != tags ? tags : new HashMap<String, String>()));
//	 } catch (FileStoreException e) {
//	 throw new EntityFileStorageException(e);
//	 }
	// }
	//
	// @Override
	// protected FileData getFromFs(String folderId, String fileId)
	// throws EntityFileStorageException {
	// try {
	// LOGGER.info("From video file storage sucessfully loaded fileId: "
	// + fileId);
	// FileData fData = FileSystemFactory.INSTANCE.getLocalFS().get(
	// folderId, fileId);
	// LOGGER.info("sucessfully loaded fileId: " + fileId);
	// return fData;
	// } catch (FileStoreException e) {
	// throw new EntityFileStorageException(e);
	// }
	// }

	@Override
	public StorageResult storeVideo(final String uid, final File file,
			final FileCategory fileCategory, final Map<String, String> tags,
			MediaType mediaType) throws EntityFileStorageException {
		return super.storeVideo(uid, file, fileCategory, tags, mediaType);
	}

	// protected IFileSystemHandler getFS() throws EntityFileStorageException {
	// return FileSystemFactory.INSTANCE.getLocalFS();
	// }

}
