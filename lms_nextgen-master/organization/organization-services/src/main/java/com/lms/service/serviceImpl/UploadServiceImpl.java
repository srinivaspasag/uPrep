package com.lms.service.serviceImpl;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.fs.exception.FileStoreException;
import com.lms.common.fs.handlers.LocalFileSystemHandler;
import com.lms.common.utils.FileUtils;
import com.lms.common.utils.ImageDisplayURLUtil;
import com.lms.common.utils.ImageFilter;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.entity.storage.OrganizationEntityFileStorage;
import com.lms.pojo.request.UploadImageReq;
import com.lms.pojo.responce.UploadImageRes;
import com.lms.service.UploadService;

@Service
public class UploadServiceImpl implements UploadService {
	private static final Logger logger = LoggerFactory.getLogger(UploadServiceImpl.class);
	@Autowired
	private LocalFileSystemHandler fileSystemHandler;
	 @Autowired
	 private OrganizationEntityFileStorage picStorage;
	@Override
	public VedantuResponse uploadImage(MultipartFile file, UploadImageReq uploadImageReq) {
		UploadImageRes uploadImageRes = null;
		
        try {
        	uploadImageReq.imageFile = picStorage.convertMultiPartToFile(file);
            uploadImageReq.folder = "organization";
            uploadImageRes = 
                    uploadContentImage(uploadImageReq);
        } catch (VedantuException e) {
            throw e;
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return new VedantuResponse(uploadImageRes);
	}
	public  UploadImageRes uploadContentImage(UploadImageReq req) throws VedantuException {

        UploadImageRes res = new UploadImageRes();
        if (!new ImageFilter().accept(new File(req.imageName))) {
            String errorMsg = "file [" + req.imageName + "] not allowed to upload as image";
            logger.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_FILE_TYPE, errorMsg);
        }
        String uuid = UUID.randomUUID().toString();
        String dstFileName = uuid + FileUtils.JPG_EXTENTION;
        //LocalFileSystemHandler tempFs = FileSystemFactory.INSTANCE.getTempFS();

        try {
        	fileSystemHandler.localFileSystemHandlerTempDirectory(false);
        	fileSystemHandler.store(req.imageFile, !StringUtils.isEmpty(req.folder) ? "images" : req.folder, dstFileName, null);
        } catch (FileStoreException e) {
        	logger.error(e.getMessage(), e);
            throw new VedantuException(VedantuErrorCode.FILE_UPLOAD_ERROR, e.getMessage());
        } finally {
            logger.debug("removing file from play temp storage : "
                    + req.imageFile.getAbsolutePath());
            req.imageFile.delete();
        }
        res.uploaded = true;
        res.uuid = uuid;
        res.filePath = getTempImageURL(dstFileName, req.folder);
        res.imgHtml = ImageDisplayURLUtil.getEmbededHtml(res.filePath, uuid);


        logger.debug("uploadContentImage response: " + res);
        return res;
    }
	 public static String getTempImageURL(String image, String folder) {
	        return ImageDisplayURLUtil.getImgHost() + "/temp/"+folder+"/" + image;
	    }
}
