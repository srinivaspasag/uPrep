package com.vedantu.content.pojos.requests.discussions;

import java.io.IOException;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.utils.IReverseImageMapperProcessor;
import com.vedantu.content.managers.DiscussionManager;
import com.vedantu.content.pojos.requests.AbstractAddContentBoardReq;

public class AddDiscussionReq extends AbstractAddContentBoardReq implements
		IReverseImageMapperProcessor {

	@Required
	public String name;

	@Required
	public String content;

	@Override
	public void addImageSrcUrl() {

	}

	@Override
	public void removeImageSrc(boolean moveImages) throws IOException,
			EntityFileStorageException {
		content = DiscussionManager.removeTempImageSrcAndSaveToFS(
				EntityType.DISCUSSION, content, true,"content");
	}

}
