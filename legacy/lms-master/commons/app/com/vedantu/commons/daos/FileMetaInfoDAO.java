package com.vedantu.commons.daos;

import org.bson.types.ObjectId;

import com.google.code.morphia.query.Query;
import com.vedantu.commons.models.mongo.FileMetaInfo;
import com.vedantu.mongo.VedantuBasicDAO;

public class FileMetaInfoDAO extends VedantuBasicDAO<FileMetaInfo, ObjectId> {
	public static final FileMetaInfoDAO INSTANCE = new FileMetaInfoDAO();

	private FileMetaInfoDAO() {
		super(FileMetaInfo.class);
		// TODO Auto-generated constructor stub
	}

	public FileMetaInfo findByFileId(String fileId) {
		Query<FileMetaInfo> query = getQuery().filter("fileId", fileId);
		return query.get();
	}
}
