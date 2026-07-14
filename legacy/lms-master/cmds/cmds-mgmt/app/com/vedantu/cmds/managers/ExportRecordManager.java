package com.vedantu.cmds.managers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.content.exports.file.writers.ExportFileWriter;
import com.vedantu.cmds.content.exports.file.writers.InfoFileWriter;
import com.vedantu.cmds.daos.CmdsContentDAO;
import com.vedantu.cmds.daos.CmdsContentLinkDAO;
import com.vedantu.cmds.daos.ExportRecordDAO;
import com.vedantu.cmds.enums.CmdsContentLinkType;
import com.vedantu.cmds.enums.ExportState;
import com.vedantu.cmds.factory.ContentExporterFactory;
import com.vedantu.cmds.mgmt.interfaces.IContentExporter;
import com.vedantu.cmds.models.CMDSContentLink;
import com.vedantu.cmds.models.ExportRecord;
import com.vedantu.cmds.models.event.details.ExportDetails;
import com.vedantu.cmds.pojos.export.EntityExportRecord;
import com.vedantu.cmds.pojos.export.EntityExportRecordInfo;
import com.vedantu.cmds.pojos.export.EntityRecord;
import com.vedantu.cmds.pojos.requests.exports.GetExportDetailsReq;
import com.vedantu.cmds.pojos.requests.exports.GetExportsReq;
import com.vedantu.cmds.pojos.requests.exports.ScheduleExportReq;
import com.vedantu.cmds.pojos.responses.DeleteExportRecordRes;
import com.vedantu.cmds.pojos.responses.GetExportDetailsRes;
import com.vedantu.cmds.pojos.responses.GetExportRecordRes;
import com.vedantu.cmds.pojos.responses.GetExportRecordsRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.daos.EntityOperationStatusDAO;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.entity.storage.AbstractEntityFileStorage;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.entity.storage.ExportRecordEntityFileStorage;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.enums.EncryptionLevel;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.OperationType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.exceptions.ExportException;
import com.vedantu.commons.exceptions.OperationAbortedException;
import com.vedantu.commons.fs.exception.FileStoreException;
import com.vedantu.commons.fs.handlers.FileSystemFactory;
import com.vedantu.commons.models.mongo.EntityOperationStatus;
import com.vedantu.commons.pojos.FileData;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.ImageDisplayURLUtil;
import com.vedantu.commons.utils.ZipHelper;
import com.vedantu.content.managers.AbstractContentManager;
import com.vedantu.content.managers.ContentSecurityManager;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.daos.OrgSectionDAO;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.models.OrgSection;
import com.vedantu.organization.models.Organization;

// import com.vedantu.commons.utils.

public class ExportRecordManager extends AbstractContentManager {

    public static final String    INFO_JSON        = "info.json";
    public static final String    CONTENTS_JSON    = "contents.json";
    public static final String    IMAGES           = EntityType.QUESTION.name().toLowerCase();
    public static final String    THUMBS           = "thumb";
    public static final String    VIDEOS           = EntityType.VIDEO.name().toLowerCase();
    public static final String    DOCUMENTS        = EntityType.DOCUMENT.name().toLowerCase();
    public static final String    FILES            = EntityType.FILE.name().toLowerCase();

    private static final ALogger  LOGGER           = Logger.of(ExportRecordManager.class);

    public ExportFileWriter       metadataFileWriter;
    public InfoFileWriter         infoWriter;
    private String                directoryPath    = null;
    public String                 exportId         = null;
    public EncryptionLevel        encryptionLevel  = EncryptionLevel.NA;
    public ContentSecurityManager securityManager  = new ContentSecurityManager();
    private String                metadataFilePath = null;

    private String                infoFilePath     = null;

    private String                zipFileName      = null;

    public ExportRecordManager(String exportId) throws ExportException {

        this.exportId = exportId;

        ExportRecord exportRecord = ExportRecordDAO.INSTANCE.getById(exportId,
                VedantuRecordState.ACTIVE);
        if (exportRecord == null) {
            throw new ExportException(VedantuErrorCode.FAILED_TO_EXPORT, "Invalid export Record");
        }
        if (exportRecord.encLevel != EncryptionLevel.NA) {
            this.encryptionLevel = exportRecord.encLevel;
        }
        directoryPath = setup(exportId);

        LOGGER.debug("Allocated library Path" + directoryPath);
        this.metadataFilePath = FileSystemFactory.INSTANCE.getTempFS().getDirectory()
                + File.separator + directoryPath + FileUtils.SEPARATOR_FWDSLASH + CONTENTS_JSON;
        File metadataJsonFile = new File(metadataFilePath);
        try {
            metadataFileWriter = new ExportFileWriter(metadataJsonFile);
        } catch (IOException e) {
            throw new ExportException(VedantuErrorCode.FAILED_TO_EXPORT,
                    "can not setup directory for export ");
        }

        this.infoFilePath = FileSystemFactory.INSTANCE.getTempFS().getDirectory()
                + FileUtils.SEPARATOR_FWDSLASH + directoryPath + FileUtils.SEPARATOR_FWDSLASH
                + INFO_JSON;
        File infoFileJson = new File(infoFilePath);
        try {
            infoWriter = new InfoFileWriter(infoFileJson);
        } catch (IOException e) {
            throw new ExportException(VedantuErrorCode.FAILED_TO_EXPORT,
                    "can not setup directory for export ");
        }
    }

    public boolean export() throws ExportException, OperationAbortedException {

        ExportRecord exportRecord = ExportRecordDAO.INSTANCE.getById(exportId,
                VedantuRecordState.ACTIVE);
        if (exportRecord == null) {
            throw new ExportException(VedantuErrorCode.FAILED_TO_EXPORT);
        }

        EntityOperationStatusDAO.INSTANCE.incCompletion(exportRecord.jobId);

        Set<EntityExportRecord> alreadyExported = new HashSet<EntityExportRecord>();

        // Export orgInfo;
        // Export orgMemberInf for targetUserId
        long succeeded = 0;
        long totalSize = 0;
        LOGGER.debug("Checking for already exported files");
        if (CollectionUtils.isNotEmpty(exportRecord.sources)) {
            for (EntityRecord currentRecord : exportRecord.sources) {

                EntityExportRecord export = null;
                if (currentRecord instanceof EntityExportRecord) {
                    export = (EntityExportRecord) currentRecord;
                } else {
                    continue;
                }
                if (!export.succeeded) {
                    continue;
                }
                IContentExporter contentExporter = ContentExporterFactory.INSTANCE
                        .get(export.content.type);

                if (contentExporter != null) {

                    boolean result = contentExporter.export(this, export, exportRecord.target);
                    if (result) {
                        succeeded++;
                        export.timeExported = System.currentTimeMillis();
                        EntityOperationStatusDAO.INSTANCE.incCompletion(exportRecord.jobId);
                    }
                }
                alreadyExported.add(export);
            }
        }

        LOGGER.debug("Checking for exporting new files");
        if (CollectionUtils.isNotEmpty(exportRecord.sources)) {
            for (EntityRecord currentRecord : exportRecord.sources) {
                EntityExportRecord record = null;
                if (currentRecord instanceof EntityExportRecord) {
                    record = (EntityExportRecord) currentRecord;
                } else {
                    continue;
                }
                if (alreadyExported.contains(record) && !record.succeeded) {
                    continue;
                }

                IContentExporter contentExporter = ContentExporterFactory.INSTANCE
                        .get(record.content.type);
                if (contentExporter != null) {
                    // EntityExportRecord entityExportRecord = new EntityExportRecord();
                    LOGGER.debug("Exporting... " + record.content);
                    try {
                        boolean result = contentExporter.export(this, record, exportRecord.target);
                        LOGGER.debug("Exporting... " + record.content + " exporting result "
                                + result);
                        record.timeExported = System.currentTimeMillis();
                        record.errorCode = StringUtils.EMPTY;
                        if (result) {
                            succeeded++;
                            record.succeeded = true;
                            totalSize += record.exportedSize;
                        }
                        metadataFileWriter.clear();
                    } catch (ExportException exception) {
                        LOGGER.error("EXPORT_FAILED " + record + " failed with exception ",
                                exception);
                        LOGGER.error("Export Record for content" + record + " for export Id "
                                + exportId, exception);
                        record.errorCode = exception.errorCode.name();
                    } finally {
                        LOGGER.debug("Export Record for content" + record + " for export Id "
                                + exportId + record);
                        ExportRecordDAO.INSTANCE.updateEntityExport(exportId, record);
                        EntityOperationStatusDAO.INSTANCE.incCompletion(exportRecord.jobId);
                    }
                }

            }
        }

        metadataFileWriter.finish();

        // initilize directory for storage
        String uuid = null;
        boolean result = ExportRecordDAO.INSTANCE.update(exportRecord._getStringId(),
                Arrays.asList(ExportState.RUNNING), ExportState.FINALIZING, false);
        if (!result) {
            throw new OperationAbortedException(VedantuErrorCode.EXPORT_ABORTED);
        }
        exportRecord.exportedSize = totalSize;

        MutableLong compressedFileSize = new MutableLong();

        infoWriter.write(exportRecord._getStringId(), succeeded,
                metadataFileWriter.getTotalRecords(), exportRecord.exportedSize);

        if ((uuid = compress(compressedFileSize)) != null) {
            exportRecord.fileId = uuid;
            exportRecord.state = ExportState.FINISHED;
            exportRecord.compressedSize = compressedFileSize.longValue();
            EntityOperationStatusDAO.INSTANCE.incCompletion(exportRecord.jobId);
            exportRecord.jobId = StringUtils.EMPTY;
            try {
                ExportRecordDAO.INSTANCE
                        .updateModel(exportRecord, Arrays.asList("fileId", "state", "jobId",
                                "exportedSize", "compressedSize"));

            } catch (VedantuException e) {
                LOGGER.error("Failed to updated export record for fileId", e);
                throw new ExportException(VedantuErrorCode.FAILED_TO_EXPORT);
            }
            return true;
        }

        return false;

    }

    private String setup(String exportRecordId) throws ExportException {

        String parentDirectory = EntityType.EXPORTRECORD.name().toLowerCase()
                + FileUtils.SEPARATOR_FWDSLASH + exportRecordId;
        boolean createdDirectory = false;
        try {
            createdDirectory = FileSystemFactory.INSTANCE.getTempFS().createParent(parentDirectory);
        } catch (FileStoreException e) {
            throw new ExportException(VedantuErrorCode.FAILED_TO_EXPORT);
        }

        if (!createdDirectory) {
            throw new ExportException(VedantuErrorCode.FAILED_TO_EXPORT);
        }

        return parentDirectory;

    }

    public boolean cleanUp() {

        try {
            FileSystemFactory.INSTANCE.getTempFS().delete(directoryPath, zipFileName);
            FileSystemFactory.INSTANCE.getTempFS().removeParent(directoryPath);
        } catch (FileStoreException e) {
            LOGGER.error("File is not removed" + zipFileName, e);

        }
        return true;
    }

    public boolean remove(ExportRecord exportRecord) {

        if (exportRecord == null) {
            return false;
        }
        if (StringUtils.isNotEmpty(exportRecord.fileId)) {
            ExportRecordEntityFileStorage storage = new ExportRecordEntityFileStorage();
            String fileName = AbstractEntityFileStorage.computeFileId(exportRecord.fileId,
                    EntityType.EXPORTRECORD, FileUtils.ZIP_EXTENTION_WITHOUT_DOT,
                    MediaType.COMPRESSED, FileCategory.ORIGINAL, null);
            if (storage.doesFileExist(EntityType.EXPORTRECORD.name(), MediaType.COMPRESSED.name(),
                    fileName)) {
                storage.remove(EntityType.EXPORTRECORD.name(), MediaType.COMPRESSED.name(),
                        fileName);
            }
        }
        return true;
    }

    /**
     * 
     * @return uuid file
     * @throws VedantuException
     */
    private String compress(MutableLong totalCompressedSize) throws ExportException {

        UUID uuid = UUID.randomUUID();
        zipFileName = uuid.toString() + FileUtils.ZIP_EXTENTION;
        String zipFilePath = FileSystemFactory.INSTANCE.getTempFS().getDirectory()
                + FileUtils.SEPARATOR_FWDSLASH + EntityType.EXPORTRECORD.name().toLowerCase()
                + FileUtils.SEPARATOR_FWDSLASH + zipFileName;

        // ZipHelper helper = new ZipHelper();

        ZipHelper helper = new ZipHelper();
        try {
            helper.zipDir(FileSystemFactory.INSTANCE.getTempFS().getDirectory()
                    + FileUtils.SEPARATOR_FWDSLASH + directoryPath, zipFilePath);
            ExportRecordEntityFileStorage storage = new ExportRecordEntityFileStorage();

            storage.store(uuid.toString(), new File(zipFilePath), MediaType.COMPRESSED,
                    FileCategory.ORIGINAL, null);
            totalCompressedSize.setValue(FileUtils.getFileSize(new File(zipFilePath)));

            return uuid.toString();

        } catch (IOException e) {
            throw new ExportException(VedantuErrorCode.SERVICE_ERROR,
                    "Export failed due to zipping it failed");
        } catch (EntityFileStorageException e) {
            throw new ExportException(VedantuErrorCode.SERVICE_ERROR,
                    "Export failed due to storage failed");
        } finally {
            try {
                LOGGER.debug("Removed file");
                FileSystemFactory.INSTANCE.getTempFS().delete(
                        EntityType.EXPORTRECORD.name().toLowerCase(),
                        uuid.toString() + FileUtils.ZIP_EXTENTION);
            } catch (FileStoreException e) {
                LOGGER.error("Could not remove file");
            }
        }

    }

    public boolean writeFile(String fileDirectory, String fileName, FileData data)
            throws ExportException {

        if (data == null || data.getIn() == null) {
            throw new ExportException(VedantuErrorCode.STORAGE_EXCEPTION);
        }

        String fileTempPath = null;
        boolean writingResult = false;
        FileOutputStream outputStream = null;
        try {
            if (StringUtils.isNotEmpty(fileDirectory)) {
                fileTempPath = directoryPath + FileUtils.SEPARATOR_FWDSLASH + fileDirectory;
            }

            if (StringUtils.isEmpty(fileName)) {
                throw new ExportException(VedantuErrorCode.STORAGE_EXCEPTION);
            }

            FileSystemFactory.INSTANCE.getTempFS().createParent(fileTempPath);

            if (FileSystemFactory.INSTANCE.getTempFS().exists(fileTempPath, fileName)) {
                return true;
            }
            String filePath = FileSystemFactory.INSTANCE.getTempFS().getFilePath(fileTempPath,
                    fileName);
            outputStream = new FileOutputStream(filePath);

            IOUtils.copy(data.getIn(), outputStream);
            writingResult = true;
        } catch (IOException e) {
            throw new ExportException(VedantuErrorCode.STORAGE_EXCEPTION);
        } catch (FileStoreException e) {
            throw new ExportException(VedantuErrorCode.STORAGE_EXCEPTION);
        } finally {

            IOUtils.closeQuietly(data.getIn());
            IOUtils.closeQuietly(outputStream);

        }

        return writingResult;
    }

    public static GetExportRecordRes start(ScheduleExportReq request) throws VedantuException {
        EntityOperationStatus status = new EntityOperationStatus();
        status.oType = OperationType.EXPORT;
        status.recordState = VedantuRecordState.TEMPORARY;
        EntityOperationStatusDAO.INSTANCE.save(status);
        ExportRecord exportRecord = new ExportRecord();
        exportRecord.state = ExportState.RUNNING;
        exportRecord.name = request.name;
        exportRecord.jobId = status._getStringId();
        exportRecord.target = request.orgEntity;
        exportRecord.recordState = VedantuRecordState.TEMPORARY;
        exportRecord.contentSrc = new SrcEntity(EntityType.ORGANIZATION, request.orgId);
        exportRecord.userId = request.userId;
        if (StringUtils.isNotEmpty(request.targetUserId)) {
            exportRecord.targetUserId = request.targetUserId;
        }
        Organization organization = OrganizationDAO.INSTANCE.getById(request.orgId);
       
        if (organization == null) {
            throw new ExportException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }
        
        exportRecord.encLevel = organization.encLevel != null ? organization.encLevel
                : EncryptionLevel.NA;

       
        LOGGER.debug("..............inside 3.................");
        MutableLong totalHits = new MutableLong();
        List<EntityExportRecord> sources = new ArrayList<EntityExportRecord>();

        if (CollectionUtils.isEmpty(request.sources)) {
            List<CMDSContentLink> contentLinks = CmdsContentLinkDAO.INSTANCE.getCmdsContentLinks(
                    null, request.orgEntity, CmdsContentLinkType.ADDED, null, Scope.ORG,
                    MongoManager.NO_START, MongoManager.NO_LIMIT, VedantuRecordState.ACTIVE,
                    totalHits);
            if (CollectionUtils.isNotEmpty(contentLinks)) {
                for (CMDSContentLink contentLink : contentLinks) {
                    // sources.add(contentLink.source);
                    sources.add(new EntityExportRecord(contentLink.source, contentLink.timeCreated));
                }
                exportRecord.total = sources.size();
            }
        } else {
            for (SrcEntity content : request.sources) {
                List<CMDSContentLink> contentLinks = CmdsContentLinkDAO.INSTANCE
                        .getCmdsContentLinks(content, request.orgEntity, CmdsContentLinkType.ADDED,
                                null, 0, 1, totalHits);
                if (CollectionUtils.isNotEmpty(contentLinks)) {
                    CMDSContentLink contentLink = contentLinks.get(0);
                    sources.add(new EntityExportRecord(contentLink.source, contentLink.timeCreated));
                    totalHits.increment();
                }

            }
        }
        LOGGER.debug("..............inside 4.................");
        exportRecord.sources = sources;

        ExportRecordDAO.INSTANCE.save(exportRecord);
        status.numOfSteps += 1 /** for initial setup */
        + totalHits.intValue() + 1 /** zipping up */
        ;
        EntityOperationStatusDAO.INSTANCE.save(status);

        EntityOperationStatusDAO.INSTANCE.updateState(status, VedantuRecordState.ACTIVE);
        ExportRecordDAO.INSTANCE.updateState(exportRecord, VedantuRecordState.ACTIVE);
        LOGGER.debug("..............inside 5.................");
        ExportDetails details = new ExportDetails();
        details.exportId = exportRecord._getStringId();
        generateEventAysc(request.userId, details, EventType.EXPORT);
        exportRecord = ExportRecordDAO.INSTANCE.getById(exportRecord._getStringId());
        GetExportRecordRes response = annotateExportRecordRes(exportRecord, false);
        LOGGER.debug("Record res" + response);
        LOGGER.debug("..............inside 6.................");
        return response;

    }

    public static GetExportRecordsRes getExports(GetExportsReq request) throws VedantuException {

        GetExportRecordsRes response = new GetExportRecordsRes();
        Set<String> sections = new HashSet<String>();
        if (StringUtils.isNotEmpty(request.sectionId)) {
            sections.add(request.sectionId);
        } else {
            List<OrgSection> orgSections = OrgSectionDAO.INSTANCE.getSectionsByOrgIds(
                    request.orgId, request.programId, request.centerId);
            if (CollectionUtils.isEmpty(orgSections)) {

                return response;
            }

            for (OrgSection section : orgSections) {
                sections.add(section._getStringId());
            }

        }

        MutableLong totalExportRecords = new MutableLong();
        List<ExportRecord> records = ExportRecordDAO.INSTANCE.getExports(sections, request.state,
                request.start, request.size, totalExportRecords);

        response.totalHits = totalExportRecords.longValue();

        for (ExportRecord record : records) {

            response.list.add(annotateExportRecordRes(record, false));
        }
        return response;

    }

    private static GetExportDetailsRes annotateExportRecordRes(ExportRecord record,
            boolean addContent) {

        GetExportDetailsRes singleRecordRes = new GetExportDetailsRes();
        singleRecordRes.jobId = record.jobId;
        singleRecordRes.recordInfo = record.toBasicInfo();
        if (StringUtils.isNotEmpty(record.fileId)) {
            singleRecordRes.url = ImageDisplayURLUtil.getEntityDownloadURL(EntityType.EXPORTRECORD,
                    record.fileId, FileUtils.ZIP_EXTENTION_WITHOUT_DOT, MediaType.COMPRESSED,
                    FileCategory.ORIGINAL, record._getStringId());

        }

        if (addContent) {
            singleRecordRes.contentInfo = new ArrayList<EntityExportRecordInfo>();
            if (CollectionUtils.isNotEmpty(record.sources)) {

                for (EntityRecord currentRecord : record.sources) {
                    EntityExportRecord entityRecord = null;
                    if (currentRecord instanceof EntityExportRecord) {
                        entityRecord = (EntityExportRecord) currentRecord;
                    } else {
                        continue;
                    }

                    VedantuBasicDAO<?, ?> dao = EntityTypeDAOFactory.INSTANCE
                            .get(entityRecord.content.type);

                    if (dao instanceof CmdsContentDAO) {
                        String name = StringUtils.EMPTY;
                        try {
                            name = ((CmdsContentDAO<?, ?>) dao).getName(entityRecord.content.id);
                        } catch (VedantuException e) {

                        }
                        singleRecordRes.contentInfo.add(new EntityExportRecordInfo(name,
                                entityRecord));
                    }

                }
            }
        }

        return singleRecordRes;
    }

    public static GetExportDetailsRes getExportDetails(GetExportDetailsReq request)
            throws VedantuException {

        if (request.size <= 0) {
            request.size = 1;
            request.fetchContent = true;
        }
        MutableLong totalExportEntityRecords = new MutableLong();

        ExportRecord record = ExportRecordDAO.INSTANCE.getExportDetails(request.exportId,
                request.start, request.size, totalExportEntityRecords);

        if (record == null) {
            throw new VedantuException(VedantuErrorCode.NO_EXPORT_RECORD_FOUND);
        }
        GetExportDetailsRes response = new GetExportDetailsRes();

        response = annotateExportRecordRes(record, request.fetchContent);
        response.totalHits = record.total;

        return response;

    }

    public static DeleteExportRecordRes cancel(GetExportDetailsReq request) throws VedantuException {

        ExportRecord exportRecord = ExportRecordDAO.INSTANCE.getById(request.exportId);

        if (exportRecord == null) {
            throw new VedantuException(VedantuErrorCode.NO_EXPORT_RECORD_FOUND);
        }

        boolean result = ExportRecordDAO.INSTANCE.update(exportRecord._getStringId(),
                Arrays.asList(ExportState.FINALIZING, ExportState.CANCELLED),
                ExportState.CANCELLED, true);
        if (!result) {
            throw new VedantuException(VedantuErrorCode.ACCESS_DENIED);
        }
        exportRecord.state = ExportState.CANCELLED;
        exportRecord.recordState = VedantuRecordState.DELETED;
        ExportRecordDAO.INSTANCE.updateModel(exportRecord, Arrays.asList("state", "recordState"));

        DeleteExportRecordRes response = new DeleteExportRecordRes();
        response.deleted = true;
        return response;

    }
}
