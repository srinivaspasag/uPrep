package com.vedantu.content.pojos.requests.comments;

import java.io.IOException;
import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.IReverseImageMapperProcessor;
import com.vedantu.content.enums.CommentType;
import com.vedantu.content.managers.CommentManager;
import com.vedantu.content.pojos.requests.AbstractAddContentReq;

public class AddCommentReq extends AbstractAddContentReq implements
		IReverseImageMapperProcessor {

	@Required
	public SrcEntity parent;

	public SrcEntity base;
	public SrcEntity root;

	public Scope scope;

	@Required
	public String content;
	public List<String> tags;
	public CommentType type;

	@Override
	public void addImageSrcUrl() {

	}

	@Override
	public void removeImageSrc(boolean moveImages) throws IOException,
			EntityFileStorageException {
		content = CommentManager.removeTempImageSrcAndSaveToFS(
				EntityType.COMMENT, content, true,"content");

	}
}
