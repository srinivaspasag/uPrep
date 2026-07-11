package com.vedantu.cmds.content.exporters;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.managers.ExportRecordManager;
import com.vedantu.cmds.models.ExportRecord;
import com.vedantu.cmds.pojos.export.EntityExportRecord;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.entity.storage.AbstractEntityFileStorage;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.ImageSize;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.entity.storage.QuestionEntityFileStorage;
import com.vedantu.commons.entity.storage.SolutionEntityFileStorage;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.exceptions.ExportException;
import com.vedantu.commons.exceptions.OperationAbortedException;
import com.vedantu.commons.pojos.FileData;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.ObjectMapperUtils;
import com.vedantu.commons.utils.image.ImageHTMLUtils;
import com.vedantu.content.daos.AnswerDAO;
import com.vedantu.content.daos.QuestionDAO;
import com.vedantu.content.daos.SolutionDAO;
import com.vedantu.content.managers.AbstractContentManager;
import com.vedantu.content.managers.ContentManager;
import com.vedantu.content.models.Answer;
import com.vedantu.content.models.ContentSearchDetails;
import com.vedantu.content.models.Question;
import com.vedantu.content.models.Solution;
import com.vedantu.content.pojos.responses.GetContentLinkRes;
import com.vedantu.content.search.details.QuestionSearchIndexDetails;
import com.vedantu.mongo.MongoManager.SortOrder;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.mongo.VedantuRecordState;

public class QuestionExporter extends AbstractContentExporter {

    public static QuestionExporter INSTANCE = new QuestionExporter();
    private static final ALogger   LOGGER   = Logger.of(QuestionExporter.class);

    @Override
    public boolean export(ExportRecordManager manager, EntityExportRecord record, SrcEntity target)
            throws ExportException, OperationAbortedException {

        boolean embedded = true;

        Question question = null;
        if (record.content.type == EntityType.CMDSQUESTION) {
            question = (Question) verifyPublishing(record.content);
            embedded = false;
        } else if (record.content.type == EntityType.QUESTION) {
            question = QuestionDAO.INSTANCE.getById(record.content.id, VedantuRecordState.ACTIVE);
        }

        return exportPublished(manager, question, target, record, embedded);
    }

    private boolean exportPublished(ExportRecordManager manager, Question question,
            SrcEntity target, EntityExportRecord entityExport, boolean embedded)
            throws ExportException, OperationAbortedException {

        if (question == null) {
            throw new ExportException(VedantuErrorCode.NOT_PUBLISHED);
        }

        ExportRecord record = getExportRecord(manager.exportId);
        FileData data = null;

        GetContentLinkRes resource = null;
        SrcEntity content = new SrcEntity(EntityType.QUESTION, question._getStringId());
        // Question question = QuestionDAO.INSTANCE.getById(content.id, VedantuRecordState.ACTIVE);

        if (!embedded) {
            try {
                resource = ContentManager.getContentLink(record.contentSrc.id, record.targetUserId,
                        content, target, UserActionType.ADDED, VedantuRecordState.ACTIVE);
            } catch (VedantuException e) {
                throw new ExportException(VedantuErrorCode.NOT_PUBLISHED, e);
            }

        } else {
            resource = new GetContentLinkRes();
            QuestionSearchIndexDetails searchIndexDetails = new QuestionSearchIndexDetails();
            searchIndexDetails.fromMongoModel(question);
            try {
                resource.content = searchIndexDetails.__getContentSearchDetails();
                AbstractContentManager.annotateExtraInfo(record.targetUserId, record.contentSrc.id,
                        EntityType.QUESTION, (ContentSearchDetails) resource.content);
            } catch (JSONException e) {
                throw new ExportException(VedantuErrorCode.FAILED_TO_EXPORT, e);
            }
            resource.target = target;
        }

        if (resource == null) {
            throw new ExportException(VedantuErrorCode.FAILED_TO_EXPORT, "es index data not found");
        }

        exportAdditionals(manager, resource, entityExport);

        Set<String> images = ImageHTMLUtils.getImageUUids(question.content);

        if (CollectionUtils.isNotEmpty(question.options)) {
            for (String option : question.options) {
                images.addAll(ImageHTMLUtils.getImageUUids(option));
            }
            if (CollectionUtils.isNotEmpty(question.imgUuids)) {
                images.addAll(question.imgUuids);
            }
        }

        QuestionEntityFileStorage storage = new QuestionEntityFileStorage();
        try {
            for (String image : images) {
                String imageFileName = AbstractEntityFileStorage.computeFileId(image,
                        EntityType.QUESTION, FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE,
                        FileCategory.CONVERTED, ImageSize.ORIGINAL);
                data = storage.getData(EntityType.QUESTION.name(), MediaType.IMAGE.getAcronym(),
                        imageFileName);
                if (data.getIn() != null) {
                    manager.writeFile(ExportRecordManager.IMAGES, imageFileName, data);
                    entityExport.exportedSize += data.getContentLength();
                }
                data.getIn().close();
            }
        } catch (IOException e) {
            throw new ExportException(VedantuErrorCode.FAILED_TO_EXPORT, e);

        } catch (EntityFileStorageException e) {
            throw new ExportException(VedantuErrorCode.FAILED_TO_EXPORT, e);
        } finally {
            if (data != null) {
                IOUtils.closeQuietly(data.getIn());
            }
        }

        if (embedded) {
            manager.metadataFileWriter.accumulate(resource);
        } else {

            manager.metadataFileWriter.writeContent(resource);

        }

        return true;
    }

    private boolean exportAdditionals(ExportRecordManager manager, GetContentLinkRes resource,
            EntityExportRecord entityExport) throws ExportException {

        if (resource.content instanceof ContentSearchDetails) {
            ContentSearchDetails contentSearchDetails = (ContentSearchDetails) resource.content;

            JSONObject info = contentSearchDetails.__getInfo();
            if (info != null) {
                info = new JSONObject();
            }
            Answer ans = AnswerDAO.INSTANCE.getQuestionAnswer(contentSearchDetails.id);
            if (ans != null) {

                try {
                    JSONObject answerJSON = new JSONObject(ObjectMapperUtils.convertValue(ans,
                            Map.class));
                    answerJSON.put("id", ans._getStringId());
                    LOGGER.debug("answerJSON: " + answerJSON);
                    contentSearchDetails.addToInfo("answer", answerJSON);
                } catch (Exception e) {

                    LOGGER.error("Failed to export answer", e);
                    throw new ExportException(VedantuErrorCode.FAILED_TO_EXPORT);
                }

            }
            // TODO solution may not be institute provided solution
            VedantuDBResult<Solution> solutionResults = SolutionDAO.INSTANCE.getSolutions(
                    contentSearchDetails.id, null, 0, 1, SortOrder.ASC);
            if (CollectionUtils.isNotEmpty(solutionResults.results)) {
                Solution solution = solutionResults.results.get(0);
                FileData data = null;
                try {
                    JSONObject solutionJSON = new JSONObject(ObjectMapperUtils.convertValue(
                            solution, Map.class));
                    solutionJSON.put("id", ans._getStringId());
                    LOGGER.debug("solution json : " + solutionJSON);

                    contentSearchDetails.addToInfo("solution", solutionJSON);
                    SolutionEntityFileStorage storage = new SolutionEntityFileStorage();
                    if (CollectionUtils.isNotEmpty(solution.imgUuids)) {
                        for (String imageUUID : solution.imgUuids) {
                            String imageFileName = AbstractEntityFileStorage.computeFileId(
                                    imageUUID, EntityType.SOLUTION,
                                    FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE,
                                    FileCategory.CONVERTED, ImageSize.ORIGINAL);
                            data = storage.getData(EntityType.SOLUTION.name(),
                                    MediaType.IMAGE.getAcronym(), imageFileName);
                            if (data.getIn() != null) {
                                manager.writeFile(ExportRecordManager.IMAGES, imageFileName, data);
                                entityExport.exportedSize += data.getContentLength();
                                data.getIn().close();
                            }

                        }
                    }

                } catch (VedantuException exception) {
                    LOGGER.error("Failed to export solution", exception);
                    throw new ExportException(VedantuErrorCode.FAILED_TO_EXPORT);
                } catch (IOException e) {
                    LOGGER.error("Failed to export solution", e);
                    throw new ExportException(VedantuErrorCode.FAILED_TO_EXPORT);
                } catch (JSONException e) {
                    LOGGER.error("Failed to export solution", e);
                    throw new ExportException(VedantuErrorCode.FAILED_TO_EXPORT);
                } finally {
                    if (data != null) {
                        IOUtils.closeQuietly(data.getIn());
                    }
                }
            }

        }
        return true;
    }
}
