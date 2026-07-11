package com.lms.pojos;

import com.lms.common.cmds.IImageUrlProcessor;
import com.lms.common.pojos.ILatexProcessor;
import com.lms.common.utils.ImageDisplayURLUtil;
import com.lms.common.utils.ImageHTMLUtils;
import com.lms.common.utils.LatexProcessor;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.entity.storage.EntityFileStorageException;
import com.lms.common.vedantu.entity.storage.FileCategory;
import com.lms.common.vedantu.entity.storage.IEntityFileStorage;
import com.lms.common.vedantu.entity.storage.fact.EntityStorageFactory;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.ImageSize;
import com.lms.common.vedantu.event.api.JSONAware;
import com.lms.interfaces.IReverseImageMapperProcessor;
import com.lms.utils.CMDSImageUtil;
import common.utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Transient;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Setter
@Getter
public class QuestionFormat implements ILatexProcessor, IImageUrlProcessor, JSONAware,
        IReverseImageMapperProcessor {

    private final Logger logger = LoggerFactory.getLogger(QuestionFormat.class);
    public Set<String> uuidImages;
    // The following two lists need to be synchronised
    public List<Integer> startIndexOfLatexOrImage;              // @TODO
    // not
    // used
    public List<Integer> endIndexOfLatexOrImage;                // @TODO
    // not
    // used
    // The text contains question with link to local link
    public String originalText;
    public String newText;
    public List<Attachment> attachments;
    @Transient
    public List<AttachmentInfo> attachmentsInfo;

    public QuestionFormat() {

        this.uuidImages = new HashSet<String>();
        this.startIndexOfLatexOrImage = new ArrayList<Integer>();
        this.endIndexOfLatexOrImage = new ArrayList<Integer>();
        this.originalText = "";
        this.newText = "";
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("QuestionFormat [uuidImages:");
        builder.append(uuidImages);
        builder.append(", startIndexOfLatexOrImage:");
        builder.append(startIndexOfLatexOrImage);
        builder.append(", endIndexOfLatexOrImage:");
        builder.append(endIndexOfLatexOrImage);
        builder.append(", originalText:");
        builder.append(originalText);
        builder.append(", newText:");
        builder.append(newText);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public void addHook() {

        if (!StringUtils.isEmpty(newText)) {
            newText = LatexProcessor.addHookToLatex(newText);
        }

    }

    @Override
    public void convertUuidsToImageUrls(boolean permanentImageUrl, String questionSetId) {

        if (uuidImages != null && !StringUtils.isEmpty(newText)) {
            for (String qUid : uuidImages) {
                String html = null;
                if (permanentImageUrl) {

                    html = CMDSImageUtil.getEmbededHtml(ImageDisplayURLUtil.getEntityImageURL(
                            EntityType.CMDSQUESTION, qUid));
                    newText = newText.replaceAll(qUid, html);
                } else {
                    html = CMDSImageUtil.toImageUrl(qUid);
                    newText = newText.replaceAll(qUid, html);
                }

            }
        }
    }

    //
    @Override
    public void convertImageUrlToUuids(boolean saveImages, boolean permanentImageUrl,
                                       String questionSetId) throws IOException {

        if (uuidImages != null && !StringUtils.isEmpty(newText)) {
            for (String qUid : uuidImages) {
                newText = permanentImageUrl ? newText.replaceAll(
                        CMDSImageUtil.toImageUrl(qUid, questionSetId), qUid) : newText.replaceAll(
                        CMDSImageUtil.toImageUrl(qUid), qUid);
                if (saveImages) {
                    // CMDSImageUtil.moveTempImageFileToPermanentLocation(
                    // CMDSImageUtil.getTempImageFile(qUid), CMDSImageUtil
                    // .getSaveToImageFile(questionSetId, qUid));
                    File imageFile = null;
                  /*  File imageFile = FileSystemFactory.INSTANCE.getTempFS()
                            .getFileWithSpecifiedName(
                                    CMDSQuestionManager.INSTANCE.getQuestionUploadDirectory(),
                                    qUid, "jpg");  */

                    Map<String, String> tags = new HashMap<String, String>();
                    tags.put("time", "" + System.currentTimeMillis());
                    tags.put("questionSetId", questionSetId);

                    try {
                        IEntityFileStorage storage = EntityStorageFactory.INSTANCE
                                .get(EntityType.CMDSQUESTION);
                        if (storage == null) {
                            logger.debug("Could not save file" + imageFile.getAbsolutePath()
                                    + " \n no associated storage found ");
                            throw new EntityFileStorageException("Storage for entity not found :"
                                    + EntityType.CMDSQUESTION);
                        }
                        storage.storeImage(qUid, imageFile, FileCategory.CONVERTED,
                                ImageSize.ORIGINAL, tags);
                    } catch (EntityFileStorageException e) {

                        logger.debug("Could not save file" + imageFile.getAbsolutePath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();
        json.put(ConstantsGlobal.NEW_TEXT, this.newText);
        json.put(ConstantsGlobal.ORIGINAL_TEXT, this.originalText);
        json.put(ConstantsGlobal.END_INDEX_OF_LATEX_OR_IMAGE, this.endIndexOfLatexOrImage);
        json.put(ConstantsGlobal.START_INDEX_OF_LATEX_OR_IMAGE, this.startIndexOfLatexOrImage);
        json.put(ConstantsGlobal.UUID_IMAGES, this.uuidImages);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        this.uuidImages = JSONUtils.getSet(json, ConstantsGlobal.UUID_IMAGES);
        this.startIndexOfLatexOrImage = JSONUtils.getIntegerList(json,
                ConstantsGlobal.START_INDEX_OF_LATEX_OR_IMAGE);
        this.endIndexOfLatexOrImage = JSONUtils.getIntegerList(json,
                ConstantsGlobal.END_INDEX_OF_LATEX_OR_IMAGE);
        this.originalText = JSONUtils.getString(json, ConstantsGlobal.ORIGINAL_TEXT);
        this.newText = JSONUtils.getString(json, ConstantsGlobal.NEW_TEXT);
    }

    @Override
    public void addImageSrcUrl() {

        if (!StringUtils.isEmpty(newText)) {
            newText = ImageHTMLUtils.addImageSrcUrl(EntityType.CMDSQUESTION, newText);
        }
    }

    @Override
    public void removeImageSrc(boolean moveImages) throws IOException, EntityFileStorageException {


        newText = null;/* AbstractContentManager.removeTempImageSrcAndSaveToFS(EntityType.CMDSQUESTION,
                newText, moveImages, "cmds"); */
        Set<String> uuids = ImageHTMLUtils.getImageUUids(newText);
        uuidImages.addAll(uuids);

    }

}
