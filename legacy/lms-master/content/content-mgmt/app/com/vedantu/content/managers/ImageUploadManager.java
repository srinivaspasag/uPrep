package com.vedantu.content.managers;

import java.io.File;
import java.util.UUID;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.fs.exception.FileStoreException;
import com.vedantu.commons.fs.handlers.FileSystemFactory;
import com.vedantu.commons.fs.handlers.LocalFileSystemHandler;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.ImageDisplayURLUtil;
import com.vedantu.commons.utils.ImageFilter;
import com.vedantu.content.pojos.requests.UploadImageReq;
import com.vedantu.content.pojos.responses.UploadImageRes;
import com.vedantu.ei.utils.StringUtils;

public class ImageUploadManager {

    private static final ALogger LOGGER = Logger.of(ImageUploadManager.class);

    public static UploadImageRes uploadContentImage(UploadImageReq req) throws VedantuException {

        UploadImageRes res = new UploadImageRes();
        if (!new ImageFilter().accept(new File(req.imageName))) {
            String errorMsg = "file [" + req.imageName + "] not allowed to upload as image";
            LOGGER.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_FILE_TYPE, errorMsg);
        }
        String uuid = UUID.randomUUID().toString();
        String dstFileName = uuid + FileUtils.JPG_EXTENTION;
        LocalFileSystemHandler tempFs = FileSystemFactory.INSTANCE.getTempFS();

        try {
            tempFs.store(req.imageFile, StringUtils.isEmpty(req.folder) ? "images" : req.folder, dstFileName, null);
        } catch (FileStoreException e) {
            LOGGER.error(e.getMessage(), e);
            throw new VedantuException(VedantuErrorCode.FILE_UPLOAD_ERROR, e.getMessage());
        } finally {
            LOGGER.debug("removing file from play temp storage : "
                    + req.imageFile.getAbsolutePath());
            req.imageFile.delete();
        }
        res.uploaded = true;
        res.uuid = uuid;
        res.filePath = getTempImageURL(dstFileName, req.folder);
        res.imgHtml = ImageDisplayURLUtil.getEmbededHtml(res.filePath, uuid);


        LOGGER.debug("uploadContentImage response: " + res);
        return res;
    }

    public static String getTempImageURL(String image, String folder) {

        return ImageDisplayURLUtil.getImgHost() + "/temp/"+folder+"/" + image;
    }
}
