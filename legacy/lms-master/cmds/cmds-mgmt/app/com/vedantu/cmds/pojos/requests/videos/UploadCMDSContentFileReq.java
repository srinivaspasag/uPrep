package com.vedantu.cmds.pojos.requests.videos;

import java.io.File;
import java.util.Map;

import play.Logger;
import play.Logger.ALogger;
import play.data.validation.Constraints.Required;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.utils.VedantuStringUtils;

/**
 * Only local api for testing locally
 * 
 * @author vikram
 * 
 */
public class UploadCMDSContentFileReq {

    static final ALogger LOGGER = Logger.of(UploadCMDSContentFileReq.class);
    // @Required
    public String        orgId;
    @Required
    public File          file;

    // @Required
    public EntityType    entityType;

    @Required
    public String        key;

    public UploadCMDSContentFileReq(MultipartFormData body) {

        LOGGER.debug("values " + body);
        FilePart videoFilePart = body.getFile("file");
        if (null != videoFilePart) {

            this.file = videoFilePart.getFile();
            //
        }
        key = _getValueFromMultipart(body.asFormUrlEncoded(), "key");
        String entityTypeString = _getValueFromMultipart(body.asFormUrlEncoded(), "entityType");
        entityType = EntityType.valueOfKey(entityTypeString);
    }

    public String validate() {

        if (null == file) {
            return "";
        }
        return null;
    }

    protected static String _getValueFromMultipart(Map<String, String[]> form, String key) {

        String[] values = form.get(key);
        if (VedantuStringUtils.isEmpty(values)) {
            return null;
        }
        String value = values[0];
        return value;
    }
}
