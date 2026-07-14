package com.vedantu.content.commons.daos;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.content.models.Video;
import com.vedantu.mongo.VedantuBasicDAO;

public class VideoDAO extends VedantuBasicDAO<Video, ObjectId>{
    private static final ALogger            LOGGER   = Logger.of(VideoDAO.class);

    public static final VideoDAO INSTANCE = new VideoDAO();

    private VideoDAO() {
        super(Video.class);
    }
    
//    public Video getVideo(String id) throws VedantuException {
//
//        Video video = getById(id);
//        if (video == null) {
//            LOGGER.error("no video found with id:" + id);
//            throw new VedantuException(VedantuErrorCode.VIDEO_NOT_FOUND,
//                    "no video found with id:" + id);
//        }
//        return video;
//    }



}
