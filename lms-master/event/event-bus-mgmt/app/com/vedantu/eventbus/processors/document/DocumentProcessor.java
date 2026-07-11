package com.vedantu.eventbus.processors.document;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.daos.CMDSDocumentDAO;
import com.vedantu.cmds.managers.CMDSDocumentManager;
import com.vedantu.cmds.models.CMDSDocument;
import com.vedantu.cmds.models.event.details.DocumentEncodingDetails;
import com.vedantu.cmds.models.event.details.VideoTranscodingDetails;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.daos.EntityOperationStatusDAO;
import com.vedantu.commons.entity.storage.AbstractEntityFileStorage;
import com.vedantu.commons.entity.storage.CMDSDocumentFileStorage;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.ImageSize;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.entity.storage.StorageResult;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.FileConversionState;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.fs.handlers.FileSystemFactory;
import com.vedantu.commons.fs.handlers.LocalFileSystemHandler;
import com.vedantu.commons.models.mongo.EntityOperationStatus;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.image.ImageGenerator;
import com.vedantu.content.utils.FileEncryptor;
import com.vedantu.content.utils.FileModelUtils;
import com.vedantu.eventbus.shell.executors.PDFThumbnailGenerator;
import com.vedantu.eventbus.shell.executors.QPDFFileLinearizer;
import com.vedantu.eventbus.shell.executors.QPDFLinearizationChecker;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;
import com.vedantu.mongo.VedantuRecordState;

public class DocumentProcessor implements IProcessor {

    private static final ALogger LOGGER = Logger.of(DocumentProcessor.class);

    @Override
    public Status process(IConsumable consumable) {

        // TODO Auto-generated method stub
        // check if file exists locally
        // get original file From server

        if (consumable == null || !(consumable instanceof IConsumable)) {
            LOGGER.debug(" Invalid event " + consumable);
            return Status.FAILURE;
        }

        Event event = (Event) consumable;

        IEventDetails details = event.fetchEventDetails();
        Logger.info("fetched eventDetails " + details);

        if (details == null || !(details instanceof DocumentEncodingDetails)) {
            LOGGER.debug(" Invalid details " + consumable + " not of type "
                    + VideoTranscodingDetails.class);
            return Status.FAILURE;
        }

        DocumentEncodingDetails documentEncodingDetails = (DocumentEncodingDetails) details;

        if (StringUtils.isEmpty(documentEncodingDetails.jobId)) {
            LOGGER.error("JobId not present");
            return Status.FAILURE;
        }

        EntityOperationStatus status = EntityOperationStatusDAO.INSTANCE.getById(
                documentEncodingDetails.jobId, VedantuRecordState.ACTIVE);
        /**
         * verify completion step count
         */
        if (status == null) {
            LOGGER.error("Job for not jobId : " + documentEncodingDetails.jobId + " present");
            return Status.FAILURE;
        }
        String jobId = status._getStringId();
        try {
            LocalFileSystemHandler tempFS = FileSystemFactory.INSTANCE.getTempFS();

            CMDSDocument document = CMDSDocumentDAO.INSTANCE.getById(documentEncodingDetails.docId);
            CMDSDocumentFileStorage storage = new CMDSDocumentFileStorage();
            try {
                String originalFileName = AbstractEntityFileStorage.computeFileId(document.uuid,
                        EntityType.CMDSDOCUMENT,
                        FileUtils.getExtensionWithoutDOT(document.originalFileName), MediaType.DOC,
                        FileCategory.ORIGINAL, null);
                File file = FileModelUtils.moveFileLocally(storage, originalFileName, document,
                        MediaType.DOC);
                if (file == null || !file.exists() || file.length() == 0) {
                    LOGGER.error("File not moved locally for processing: ");
                    return Status.FAILURE;
                }

                Map<String, String> vedantuMetadata = new HashMap<String, String>();
                if (document.contentSrc != null) {
                    vedantuMetadata.put(ConstantsGlobal.ORG_ID, document.contentSrc.id);
                }
                vedantuMetadata.put(ConstantsGlobal.DOC_ID, document._getStringId());
                StorageResult result = null;
                File outputPDFFile = null;
                if (!document.extension.equalsIgnoreCase(FileUtils.PDF_EXTENSTION_WITHOUT_DOT)
                        && documentEncodingDetails.convertToPDF) {

                    outputPDFFile = tempFS.getFileWithSpecifiedName(EntityType.CMDSDOCUMENT.name()
                            .toLowerCase(), document.uuid, FileUtils.PDF_EXTENSTION_WITHOUT_DOT);
                    PDFConvertor convertor = PDFConvertor.getInstance();

                    if (!convertor.convertToPdf(file, outputPDFFile) || !outputPDFFile.exists()) {
                        LOGGER.debug("Failed while converting to pdf");
                        return Status.FAILURE;
                    }

                    EntityOperationStatusDAO.INSTANCE.incCompletion(jobId); // step 1
                    // FileUtils.deleteFile(file.getName(), file);

                } else if (document.extension
                        .equalsIgnoreCase(FileUtils.PDF_EXTENSTION_WITHOUT_DOT)) {
                    outputPDFFile = file;
                } else {
                    LOGGER.debug("Failed document to pdf generation" + file.getAbsolutePath());
                    return Status.FAILURE;
                }

                File linearizedPDFFile = null;
                if (documentEncodingDetails.generateLinearizedPDF) {
                    // generate LINEARIZED DOCUMENT
                    QPDFLinearizationChecker checker = new QPDFLinearizationChecker();
                    if (!checker.check(outputPDFFile)) {

                        linearizedPDFFile = tempFS.getFileWithSpecifiedName(EntityType.CMDSDOCUMENT
                                .name().toLowerCase(), UUID.randomUUID().toString(),
                                FileUtils.PDF_EXTENSTION_WITHOUT_DOT);
                        QPDFFileLinearizer linearizer = new QPDFFileLinearizer();
                        boolean hasLinearizedPDFGenerated = false;
                        try {
                            hasLinearizedPDFGenerated = linearizer.convert(outputPDFFile,
                                    linearizedPDFFile);
                        } catch (VedantuException e) {

                            LOGGER.error("Lineared pdf  generation failed " + jobId + " & eventId "
                                    + outputPDFFile.getAbsolutePath());
                        }
                        if (!hasLinearizedPDFGenerated || !linearizedPDFFile.exists()) {
                            LOGGER.debug("Failed to linearize : " + outputPDFFile.getAbsolutePath());
                            return Status.FAILURE;
                        }
                    } else {
                        LOGGER.debug("Already linearized pdf " + outputPDFFile.getAbsolutePath());
                        linearizedPDFFile = outputPDFFile;
                    }

                    try {
                        result = storage.store(document.uuid, linearizedPDFFile, MediaType.DOC,
                                FileCategory.CONVERTED, vedantuMetadata);

                        document.states.add(FileConversionState.CONVERTED);
                        document.extension = FileUtils.PDF_EXTENSTION_WITHOUT_DOT;
                        // CMDSDocumentDAO.INSTANCE.save(document);
                        CMDSDocumentDAO.INSTANCE.updateModel(document,
                                Arrays.asList("extension", "states"));

                    } finally {

                        // CMDSDocumentDAO.INSTANCE.save(document);
                        EntityOperationStatusDAO.INSTANCE.incCompletion(jobId); // step 2
                    }

                    // FileUtils.deleteFile(outputPDFFile.getName(), outputPDFFile);

                    if (StringUtils.isNotEmpty(document.passphrase)) {
                        LOGGER.debug(" passphrase provided so will do encryption");
                        try {
                            FileEncryptor.encrypt(storage, document, linearizedPDFFile,
                                    MediaType.DOC);
                            document.states.add(FileConversionState.ENCRYPTED);
                            CMDSDocumentDAO.INSTANCE.updateModel(document, Arrays.asList("states"));
                        } finally {

                            // CMDSDocumentDAO.INSTANCE.save(document);
                            EntityOperationStatusDAO.INSTANCE.incCompletion(jobId); // step 3
                        }
                    }

                }

                if (documentEncodingDetails.generateThumbnail && linearizedPDFFile != null
                        && linearizedPDFFile.exists()) {

                    PDFThumbnailGenerator thumbnailFileGenerator = new PDFThumbnailGenerator();
                    // generate LINEARIZED DOCUMENT
                    File outputFirstPageImage = tempFS.getFileWithSpecifiedName(
                            EntityType.CMDSDOCUMENT.name().toLowerCase(), UUID.randomUUID()
                                    .toString(), FileUtils.JPG_EXTENTION_WITHOUT_DOT);
                    boolean hasFirstPageImageGenerated = false;
                    try {
                        hasFirstPageImageGenerated = thumbnailFileGenerator.convert(
                                linearizedPDFFile, outputFirstPageImage);
                    } catch (VedantuException e) {

                        LOGGER.error("First page image generated failed " + jobId + " & eventId "
                                + file.getAbsolutePath());
                    }
                    if (!hasFirstPageImageGenerated || !outputFirstPageImage.exists()) {
                        LOGGER.debug("Failed page image generation" + file.getAbsolutePath());
                        return Status.FAILURE;
                    }

                    for (ImageSize imageSize : new ImageSize[] { ImageSize.MEDIUM, ImageSize.SMALL,
                            ImageSize.EXTRA_SMALL }) {
                        File convertedFile = ImageGenerator.createImage(outputFirstPageImage,
                                imageSize, outputFirstPageImage.getName());

                        result = storage.storeImage(document._getStringId(), convertedFile,
                                FileCategory.CONVERTED, imageSize, null);
                        LOGGER.debug(result.toString());
                        document.thumbnail = document._getStringId();
                        // FileUtils.deleteFile(convertedFile.getName(), convertedFile);
                    }

                    // CMDSDocumentDAO.INSTANCE.save(document);
                    CMDSDocumentDAO.INSTANCE.updateModel(document, Arrays.asList("thumbnail"));
                    EntityOperationStatusDAO.INSTANCE.incCompletion(jobId); // step 4
                }

                CMDSDocumentManager.generateEventAysc(document.userId, document,
                        EventActionType.ADD, EventType.INDEX_CMDS_DOCUMENT, UserActionType.ADDED,
                        false);
                document.converted = true;
                CMDSDocumentDAO.INSTANCE.updateModel(document, Arrays.asList("converted"));
            } catch (VedantuException e) {
                LOGGER.error("Can not reindex document", e);

                return Status.FAILURE;
            }

            return Status.SUCCESS;

        } catch (Exception e) {

            LOGGER.error(
                    "Conversion failed for jobId " + jobId + " & eventId " + event._getStringId(),
                    e);
            EntityOperationStatusDAO.INSTANCE.updateErrorCode(jobId,
                    VedantuErrorCode.CONVERSION_FAILED.name());
        }

        return Status.FAILURE;
    }

}
