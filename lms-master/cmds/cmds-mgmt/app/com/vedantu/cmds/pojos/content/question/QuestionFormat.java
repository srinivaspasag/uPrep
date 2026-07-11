package com.vedantu.cmds.pojos.content.question;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.annotations.Transient;
import com.vedantu.cmds.commons.util.LatexProcessor;
import com.vedantu.cmds.managers.CMDSQuestionManager;
import com.vedantu.cmds.mgmt.interfaces.IImageUrlProcessor;
import com.vedantu.cmds.mgmt.interfaces.ILatexProcessor;
import com.vedantu.cmds.utils.CMDSImageUtil;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.entity.factory.EntityStorageFactory;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.IEntityFileStorage;
import com.vedantu.commons.entity.storage.ImageSize;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.events.apis.JSONAware;
import com.vedantu.commons.fs.handlers.FileSystemFactory;
import com.vedantu.commons.utils.IReverseImageMapperProcessor;
import com.vedantu.commons.utils.ImageDisplayURLUtil;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.commons.utils.image.ImageHTMLUtils;
import com.vedantu.content.managers.AbstractContentManager;
import com.vedantu.content.pojos.Attachment;
import com.vedantu.content.pojos.AttachmentInfo;

public class QuestionFormat implements ILatexProcessor, IImageUrlProcessor, JSONAware,
        IReverseImageMapperProcessor {

    private static final ALogger LOGGER = Logger.of(OptionFormat.class);
    public Set<String>          uuidImages;
    // The following two lists need to be synchronised
    public List<Integer>         startIndexOfLatexOrImage;              // @TODO
                                                                         // not
                                                                         // used
    public List<Integer>         endIndexOfLatexOrImage;                // @TODO
                                                                         // not
                                                                         // used
    // The text contains question with link to local link
    public String                originalText;
    public String                newText;
    public List<Attachment>      attachments;

    @Transient
    public List<AttachmentInfo>      attachmentsInfo;

    public QuestionFormat() {

        this.uuidImages = new HashSet<String>();
        this.startIndexOfLatexOrImage = new ArrayList<Integer>();
        this.endIndexOfLatexOrImage = new ArrayList<Integer>();
        this.originalText = new String();
        this.newText = new String();
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

        if (StringUtils.isNotEmpty(newText)) {
            newText = LatexProcessor.addHookToLatex(newText);
        }

    }

    @Override
    public void convertUuidsToImageUrls(boolean permanentImageUrl, String questionSetId) {

        if (uuidImages != null && StringUtils.isNotEmpty(newText)) {
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

        if (uuidImages != null && StringUtils.isNotEmpty(newText)) {
            for (String qUid : uuidImages) {
                newText = permanentImageUrl ? newText.replaceAll(
                        CMDSImageUtil.toImageUrl(qUid, questionSetId), qUid) : newText.replaceAll(
                        CMDSImageUtil.toImageUrl(qUid), qUid);
                if (saveImages) {
                    // CMDSImageUtil.moveTempImageFileToPermanentLocation(
                    // CMDSImageUtil.getTempImageFile(qUid), CMDSImageUtil
                    // .getSaveToImageFile(questionSetId, qUid));
                    File imageFile = FileSystemFactory.INSTANCE.getTempFS()
                            .getFileWithSpecifiedName(
                                    CMDSQuestionManager.INSTANCE.getQuestionUploadDirectory(),
                                    qUid, "jpg");

                    Map<String, String> tags = new HashMap<String, String>();
                    tags.put("time", "" + System.currentTimeMillis());
                    tags.put("questionSetId", questionSetId);

                    try {
                        IEntityFileStorage storage = EntityStorageFactory.INSTANCE
                                .get(EntityType.CMDSQUESTION);
                        if (storage == null) {
                            LOGGER.debug("Could not save file" + imageFile.getAbsolutePath()
                                    + " \n no associated storage found ");
                            throw new EntityFileStorageException("Storage for entity not found :"
                                    + EntityType.CMDSQUESTION);
                        }
                        storage.storeImage(qUid, imageFile, FileCategory.CONVERTED,
                                ImageSize.ORIGINAL, tags);
                    } catch (EntityFileStorageException e) {

                        LOGGER.debug("Could not save file" + imageFile.getAbsolutePath());
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

        if (StringUtils.isNotBlank(newText)) {
            newText = ImageHTMLUtils.addImageSrcUrl(EntityType.CMDSQUESTION, newText);
        }
    }

    @Override
    public void removeImageSrc(boolean moveImages) throws IOException, EntityFileStorageException {


        newText = AbstractContentManager.removeTempImageSrcAndSaveToFS(EntityType.CMDSQUESTION,
                newText, moveImages, "cmds");
        Set<String> uuids = ImageHTMLUtils.getImageUUids(newText);
        uuidImages.addAll(uuids);

    }

}
