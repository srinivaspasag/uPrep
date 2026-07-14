package com.vedantu.cmds.pojos.content.question;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;

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

public class OptionFormat implements ILatexProcessor, IImageUrlProcessor, JSONAware,
        IReverseImageMapperProcessor {

    private static final ALogger LOGGER = Logger.of(OptionFormat.class);
    public Set<String>           uuidImages;
    // The following two lists need to be synchronised
    public List<Integer>         startIndexOfLatexOrImage;
    public List<Integer>         endIndexOfLatexOrImage;
    // The text contains question with link to local link
    public List<String>          originalOptions;
    public List<String>          newOptions;
    public List<String>          optionOrder;

    public OptionFormat() {

        this.uuidImages = new HashSet<String>();
        this.startIndexOfLatexOrImage = new ArrayList<Integer>();
        this.endIndexOfLatexOrImage = new ArrayList<Integer>();
        this.originalOptions = new ArrayList<String>();
        this.newOptions = new ArrayList<String>();
        this.optionOrder = new ArrayList<String>();
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("OptionFormat [uuidImages:");
        builder.append(uuidImages);
        builder.append(", startIndexOfLatexOrImage:");
        builder.append(startIndexOfLatexOrImage);
        builder.append(", endIndexOfLatexOrImage:");
        builder.append(endIndexOfLatexOrImage);
        builder.append(", originalOptions:");
        builder.append(originalOptions);
        builder.append(", newOptions:");
        builder.append(newOptions);
        builder.append(", optionOrder:");
        builder.append(optionOrder);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public void addHook() {

        if (CollectionUtils.isNotEmpty(newOptions)) {
            List<String> finalOption = new ArrayList<String>();
            for (String option : this.newOptions) {
                finalOption.add(LatexProcessor.addHookToLatex(option));
            }

            this.newOptions = finalOption;
        }

    }

    @Override
    public void convertUuidsToImageUrls(boolean permanentImageUrl, String questionSetId) {

        if (uuidImages != null && newOptions != null) {
            for (String opnUid : uuidImages) {
                List<String> newOptions = new ArrayList<String>();
                LOGGER.debug("Option format from uuid to image urls " + opnUid);
                for (String nOption : this.newOptions) {
                    if (permanentImageUrl) {
                        // CMDSImageUtil
                        // .toImageUrl(opnUid, questionSetId)
                        String html = CMDSImageUtil.getEmbededHtml(ImageDisplayURLUtil
                                .getEntityImageURL(EntityType.CMDSQUESTION, opnUid));
                        newOptions.add(nOption.replaceAll(opnUid, html));
                    } else {
                        newOptions
                                .add(nOption.replaceAll(opnUid, CMDSImageUtil.toImageUrl(opnUid)));
                    }
                }
                this.newOptions = newOptions;
            }
        }
    }

    @Override
    public void convertImageUrlToUuids(boolean saveImages, boolean permanentImageUrl,
            String questionSetId) throws IOException {

        if (uuidImages != null && newOptions != null) {
            for (String opnUid : uuidImages) {
                LOGGER.debug("Option format from imageurl to uuid ");
                List<String> newOptions = new ArrayList<String>();
                for (String nOption : this.newOptions) {
                    nOption = nOption.replaceAll(CMDSImageUtil.toImageUrl(opnUid), opnUid);
                    newOptions.add(nOption);
                    if (saveImages) {
                        File imageFile = FileSystemFactory.INSTANCE.getTempFS()
                                .getFileWithSpecifiedName(
                                        CMDSQuestionManager.INSTANCE.getQuestionUploadDirectory(),
                                        opnUid, "jpg");

                        Map<String, String> tags = new HashMap<String, String>();
                        tags.put("time", "" + System.currentTimeMillis());
                        if (StringUtils.isNotEmpty(questionSetId)) {
                            tags.put("questionSetId", questionSetId);
                        }

                        try {
                            IEntityFileStorage storage = EntityStorageFactory.INSTANCE
                                    .get(EntityType.CMDSQUESTION);
                            if (storage == null) {
                                LOGGER.debug("Could not save file" + imageFile.getAbsolutePath()
                                        + " \n no associated storage found ");
                                throw new EntityFileStorageException(
                                        "Storage for entity not found :" + EntityType.CMDSQUESTION);
                            }
                            storage.storeImage(opnUid, imageFile, FileCategory.CONVERTED,
                                    ImageSize.ORIGINAL, tags);
                        } catch (EntityFileStorageException e) {
                            LOGGER.debug("Could not save file" + imageFile.getAbsolutePath());
                        }
                        // CMDSImageUtil.moveTempImageFileToPermanentLocation(
                        // CMDSImageUtil.getTempImageFile(opnUid),
                        // CMDSImageUtil.getSaveToImageFile(questionSetId,
                        // opnUid));
                    }
                }
                this.newOptions = newOptions;
            }
        }
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();
        json.put(ConstantsGlobal.UUID_IMAGES, uuidImages);
        json.put("startIndexOfLatexOrImage", startIndexOfLatexOrImage);
        json.put("endIndexOfLatexOrImage", endIndexOfLatexOrImage);
        json.put("originalOptions", originalOptions);
        json.put("newOptions", newOptions);
        json.put("optionOrder", optionOrder);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        this.uuidImages = JSONUtils.getSet(json, ConstantsGlobal.UUID_IMAGES);
        this.startIndexOfLatexOrImage = JSONUtils.getIntegerList(json, "startIndexOfLatexOrImage");
        this.endIndexOfLatexOrImage = JSONUtils.getIntegerList(json, "endIndexOfLatexOrImage");
        this.originalOptions = JSONUtils.getList(json, "originalOptions");
        this.newOptions = JSONUtils.getList(json, "newOptions");
        this.optionOrder = JSONUtils.getList(json, "optionOrder");
    }

    @Override
    public void addImageSrcUrl() {

        if (CollectionUtils.isNotEmpty(this.newOptions)) {
            List<String> newOptions = new ArrayList<String>();

            for (String nOption : this.newOptions) {
                String newOption = ImageHTMLUtils.addImageSrcUrl(EntityType.CMDSQUESTION, nOption);
                newOptions.add(newOption);

            }
            this.newOptions = newOptions;
        }

    }

    @Override
    public void removeImageSrc(boolean moveImages) throws IOException, EntityFileStorageException {

        LOGGER.debug("Option format from imageurl to uuid ");

        List<String> newOptions = new ArrayList<String>();
        if (CollectionUtils.isNotEmpty(this.newOptions)) {
            for (String nOption : this.newOptions) {
                nOption = AbstractContentManager.removeTempImageSrcAndSaveToFS(
                        EntityType.CMDSQUESTION, nOption, moveImages, "cmds");
                Set<String> uuids = ImageHTMLUtils.getImageUUids(nOption);
                this.uuidImages.addAll(uuids);
                // nOption = nOption.replaceAll(CMDSImageUtil.toImageUrl(opnUid), opnUid);
                newOptions.add(nOption);
                // if (saveImages) {
                // File imageFile = FileSystemFactory.INSTANCE.getTempFS()
                // .getFileWithSpecifiedName(
                // CMDSQuestionManager.INSTANCE.getQuestionUploadDirectory(),
                // opnUid, "jpg");
                //
                // Map<String, String> tags = new HashMap<String, String>();
                // tags.put("time", "" + System.currentTimeMillis());
                // if (StringUtils.isNotEmpty(questionSetId)) {
                // tags.put("questionSetId", questionSetId);
                // }
                //
                // try {
                // IEntityFileStorage storage = EntityStorageFactory.INSTANCE
                // .get(EntityType.CMDSQUESTION);
                // if (storage == null) {
                // LOGGER.debug("Could not save file" + imageFile.getAbsolutePath()
                // + " \n no associated storage found ");
                // throw new EntityFileStorageException("Storage for entity not found :"
                // + EntityType.CMDSQUESTION);
                // }
                // storage.storeImage(opnUid, imageFile, FileCategory.CONVERTED,
                // ImageSize.ORIGINAL, tags);
                // } catch (EntityFileStorageException e) {
                // LOGGER.debug("Could not save file" + imageFile.getAbsolutePath());
                // }
                // // CMDSImageUtil.moveTempImageFileToPermanentLocation(
                // // CMDSImageUtil.getTempImageFile(opnUid),
                // // CMDSImageUtil.getSaveToImageFile(questionSetId,
                // // opnUid));
                // }
            }
            this.newOptions = newOptions;
        }
    }

}
