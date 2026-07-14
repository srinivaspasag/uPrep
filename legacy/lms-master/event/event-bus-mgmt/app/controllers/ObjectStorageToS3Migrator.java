package controllers;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import play.Logger;
import play.Logger.ALogger;
import play.Play;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.fs.exception.FileStoreException;
import com.vedantu.commons.fs.handlers.ObjectStorageHandler;
import com.vedantu.commons.fs.handlers.S3Handler;
import com.vedantu.commons.fs.objectstorage.ObjectFileEx;
import com.vedantu.eventbus.requests.MigrateReq;

public class ObjectStorageToS3Migrator extends AbstractVedantuController {

    private static ALogger LOGGER = Logger.of(ObjectStorageToS3Migrator.class);

    public static Result migrate() {

        MigrateReq request = null;
        Form<MigrateReq> requestForm = Form.form(MigrateReq.class).bindFromRequest();

        LOGGER.debug("Request " + requestForm.data());

        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        request = requestForm.get();
        
        if (request == null || request.type == EntityType.UNKNOWN) {
            return notFound();
        }

        LOGGER.debug(" Called migrations for  type :" + request.type);
        String fwkId = Play.application().configuration().getString("fwkId");

        ObjectStorageHandler osHanlder = new ObjectStorageHandler();
        String objectStorageContainer = osHanlder.getParentName(request.type, fwkId);

        S3Handler s3Handler = new S3Handler();
        String s3Bucket = s3Handler.getParentName(request.type, fwkId);

        Set<String> moveFailedSet = new HashSet<String>();
        List<ObjectFileEx> objectFiles;
        try {
            objectFiles = osHanlder.getParentContent(objectStorageContainer);
        } catch (FileStoreException e) {
            LOGGER.debug("No input source found " + objectStorageContainer, e);
            return notFound();
        }

        if (CollectionUtils.isNotEmpty(objectFiles)) {
            LOGGER.debug("Total number of files to be moved" + objectFiles.size());
            
            for (ObjectFileEx objectFile : objectFiles) {
                InputStream stream;
                try {
                    LOGGER.error("Moving file" + objectFile.getName());
                    if (s3Handler.exists(s3Bucket, objectFile.getName())) {
                        LOGGER.error("Can not move file as file already exists "
                                + objectFile.getName());
                        continue;
                    }
                    stream = objectFile.getStream();
                    if (stream == null) {
                        LOGGER.error("Can not move file as no filestream found "
                                + objectFile.getName());
                        continue;
                    }
                    // s3Handler.store(s3Bucket, objectFile.getName(), stream,
                    // osHanlder.getUserMetadata(objectFile), objectFile.getStream().available());

                } catch (Exception e) {
                    LOGGER.error("Can not move file" + objectFile.getName(), e);
                    moveFailedSet.add(objectFile.getName());
                }

            }
        }
        return ok();

    }
}