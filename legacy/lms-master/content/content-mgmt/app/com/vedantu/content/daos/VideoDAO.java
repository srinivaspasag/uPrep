package com.vedantu.content.daos;

import java.util.List;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.content.interfaces.IDownloadable;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.content.models.Video;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.user.daos.AbstractUserActionDAO;

public class VideoDAO extends AbstractUserActionDAO<Video, ObjectId> implements IDownloadable {

    public static final VideoDAO INSTANCE = new VideoDAO();
    private static final ALogger LOGGER   = Logger.of(VideoDAO.class);

    public VideoDAO() {

        super(Video.class);
        // TODO Auto-generated constructor stub
    }

    public Video getVideo(String id) throws VedantuException {

        Video video = getById(id);
        if (video == null) {
            LOGGER.error("no video found with id:" + id);
            throw new VedantuException(VedantuErrorCode.VIDEO_NOT_FOUND,
                    "no video found with id:" + id);
        }
        return video;
    }

    public Video getByCMDSVideoId(String id) {
        Video video = getQuery().filter("cmdsVideoId", id).get();
        if(video == null){
            LOGGER.error("Cannot find video with the cmds video id :" + id);
        }
        return video;
    }

    @Override
    public String getDownloadName(String id, VedantuBaseMongoModel record) {

        Video currentRecord = null;

        if (record == null) {
            currentRecord = getById(id);
        } else {
            if (record instanceof Video) {
                currentRecord = (Video) record;
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
