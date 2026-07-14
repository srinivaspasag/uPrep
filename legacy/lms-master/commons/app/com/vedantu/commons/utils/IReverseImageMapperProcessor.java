package com.vedantu.commons.utils;

import java.io.IOException;

import com.vedantu.commons.entity.storage.EntityFileStorageException;

public interface IReverseImageMapperProcessor {

	public void addImageSrcUrl();

	public void removeImageSrc(boolean moveImages) throws IOException,
			EntityFileStorageException;
}
