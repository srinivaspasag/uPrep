package com.vedantu.content.daos;

import java.util.List;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.content.interfaces.IDownloadable;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.content.models.File;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.user.daos.AbstractUserActionDAO;

public class FileDAO extends AbstractUserActionDAO<File, ObjectId> implements IDownloadable {

    public static final FileDAO  INSTANCE = new FileDAO();
    private static final ALogger LOGGER   = Logger.of(FileDAO.class);

    public FileDAO() {

        super(File.class);
        // TODO Auto-generated constructor stub
    }

    public File getFile(String id) throws VedantuException {

        File file = getById(id);
        if (file == null) {
            LOGGER.error("no question found with id:" + id);
            throw new VedantuException(VedantuErrorCode.DOCUMENT_NOT_FOUND,
                    "no question found with id:" + id);
        }
        return file;
    }

    @Override
    public String getDownloadName(String id, VedantuBaseMongoModel record) {

        File currentRecord = null;

        if (record == null) {
            currentRecord = getById(id);
        } else {
            if (record instanceof File) {
                currentRecord = (File) record;
            }
        }

        return FileUtils.getFileName(currentRecord.originalFileName);
    }

    @Override
    public List<SrcEntity> getChildren(String id) {

        // TODO Auto-generated method stub
        return null;
    }

}
