package com.vedantu.ext.cmds.managers;

import com.vedantu.ext.cmds.db.datamanagers.*;
import com.vedantu.ext.cmds.db.models.*;
import com.vedantu.ext.cmds.enums.EntityType;
import com.vedantu.ext.cmds.enums.JobState;
import com.vedantu.ext.cmds.export.models.SDCardChecksum;
import com.vedantu.ext.cmds.export.models.SDCardFileMetadata;
import com.vedantu.ext.cmds.export.models.SDCardGroupMetadata;
import com.vedantu.ext.cmds.export.models.SDCardMetadata;
import com.vedantu.ext.cmds.utils.commons.FileUtils;
import com.vedantu.ext.cmds.utils.commons.StringUtils;
import com.vedantu.ext.cmds.utils.config.Config;

import javax.servlet.ServletException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PackageManager extends AbstractManager implements Runnable {

    private static final String V_SDCARD_GROUP_FILE  = ".vg";
    private static final String V_SDCARD_FILE        = ".vs";
    private static final String V_FILE_METADATA_FILE = ".vfmd";
    private static final String V_VERIFICATION_FILE  = ".vv";

    private SDCard              card;
    private String              location;
    private int                 jobId;

    public PackageManager(SDCard sdCard, String parentDirectory, int jobId) {

        this.card = sdCard;
        this.location = parentDirectory;
        this.jobId = jobId;
    }

    @SuppressWarnings("resource")
    public void run() {

        Job job = JobInfoDataManager.INSTANCE.getJobInfo(jobId);
        Organization organization = OrgDataManager.INSTANCE.getOrganization();

        if (job == null) {
            LOGGER.debug("No job found for id " + jobId);
            return;
        }

        try {

            LOGGER.debug("Cheking for groups and sd card directory" + location);

            SDCardGroup group = SDCardGroupDataManager.INSTANCE.getSDCardGroup(card.groupId);

            if (group == null) {
                job.status = JobState.FAILED.name();
                JobInfoDataManager.INSTANCE.update(job);
                return;
            }
         
            File sdCardDirectory = new File(location + File.separator
                    + StringUtils.strip((StringUtils.isNotEmpty(card.name) ? card.name : card.id))
                    + File.separator + "learnpedia");
            LOGGER.debug("SD Card directory creupsertating if necessary "
                    + sdCardDirectory.getAbsolutePath());
            if (!sdCardDirectory.exists()) {
                sdCardDirectory.mkdirs();
            } else {
                boolean deleteResult = FileUtils.deleteRecursive(sdCardDirectory);
                if (deleteResult) {
                    LOGGER.debug("Emptied directory " + sdCardDirectory.getAbsolutePath());
                    sdCardDirectory.mkdirs();

                } else {
                    throw new Exception("Unabled to delete directory "
                            + sdCardDirectory.getAbsolutePath());
                }
                Thread.sleep(10000);
            }

            int resourceCount = ResourceDataManager.INSTANCE.getResourceCount(card.id,
                    EntityType.SDCARD.name());
            LOGGER.debug("SD Card found sd cardupsert resources count " + resourceCount);

          

            LocalFileSystemHandler handler = new LocalFileSystemHandler(
                    sdCardDirectory.getAbsolutePath());

      
            FlashRecordInfo recordInfo = FlashRecordDataManager.INSTANCE.getFlashRecord(
                    card.orgKeyId, card.groupId, card.id, group.targetId);
            if (recordInfo == null) {
                LOGGER.debug("Record info not found so inserting now");
                 recordInfo = new FlashRecordInfo(card.orgKeyId, group.targetId,
                        card.groupId, card.id);
                 FlashRecordDataManager.INSTANCE.upsert(recordInfo);
            }

            LOGGER.debug("Checking if sd card is synched ");
            int fileDownloadInfoBefore = FileDownloadInfoDataManager.INSTANCE
                    .getFileDownloadInfoCount(card.id, EntityType.SDCARD.name());
            SDCardSyncManager manager = new SDCardSyncManager(card.id, EntityType.SDCARD.name(),
                    card.orgKeyId);
            manager.sync();
          
            int fileDownloadInfoAfter= FileDownloadInfoDataManager.INSTANCE
                    .getFileDownloadInfoCount(card.id, EntityType.SDCARD.name());
            job.steps+= fileDownloadInfoAfter- fileDownloadInfoBefore;
            JobInfoDataManager.INSTANCE.update(job, Job.FIELD_STEPS);
            JobInfoDataManager.INSTANCE.increment(job._id, resourceCount);
            try {
                File contentMetadataFile = new File(sdCardDirectory.getAbsolutePath(),
                        V_FILE_METADATA_FILE);
                JSONFileWriter writer = new JSONFileWriter(contentMetadataFile, organization.key);

                // for (Resource resource : resources) {
                // LOGGER.debug("Checking for resource " + resource.id + " " + resource.type);
                List<FileDownloadInfo> infos = FileDownloadInfoDataManager.INSTANCE
                    .getFileDownloadInfoFromTarget(card.id, EntityType.SDCARD.name());

                if (null == infos || infos.size() == 0) {
                    LOGGER.debug("Files for content not found");
                    job.status = JobState.FAILED.name();
                    JobInfoDataManager.INSTANCE.update(job, Job.FIELD_STATUS);
                    return;
                }
                for (FileDownloadInfo info : infos) {
                    // TODO update status somewhere
                    if (info.location == null || info.location.isEmpty()) {
                        info.location = Config.DESKTOP_LOCATION + File.separator + "desktop_"
                                + info.entityType.toLowerCase();
                    }
                    String entityTypeRelativePath = info.entityType.toLowerCase();
                    LOGGER.debug("Checking for file" + info.location + " "
                            + sdCardDirectory.getAbsolutePath() + " " + info.name);
                    boolean copyResult = handler.copy(info.location,
                            sdCardDirectory.getAbsolutePath() + File.separator
                                    + entityTypeRelativePath, info.name, info.name);

                    if (copyResult) {
                        LOGGER.debug("Successfully copied file");
                    } else {
                        LOGGER.error("Unable to copied file");
                        throw new ServletException("Unable to copy file");
                    }
                    SDCardFileMetadata data = new SDCardFileMetadata(info.entityId,
                            info.entityType, info.name, entityTypeRelativePath, card.contentSize,
                            card.id, card.name);
                    LOGGER.debug(data.toJSON().toString());
                    writer.write(data.toJSON().toString(), true);
                    writer.flush();
                    JobInfoDataManager.INSTANCE.increment(jobId, 1);

                }

                writer.close();

/*                //copying test results file to sdcard flder
                boolean testsCopyResult = handler.copy(Config.DESKTOP_LOCATION + File.separator +"test",
                        sdCardDirectory.getAbsolutePath() + File.separator
                                + "TEST", "Test_data", "Test_data");

                if (testsCopyResult) {
                    LOGGER.debug("Successfully copied file");
                } else {
                    LOGGER.error("Unable to copied file");
                    throw new ServletException("Unable to copy file");
                }*/
                File srcDir  = new File(Config.DESKTOP_LOCATION + File.separator +"test");
                File destDir = new File(sdCardDirectory.getAbsolutePath() + File.separator
                        + "TEST");

                FileUtils.copyDirectory(srcDir, destDir);

                //copying content links file and module links file to sdcard folder
                boolean contentLinkCopyResult = handler.copy(Config.DESKTOP_LOCATION + File.separator +"LINKS",
                        sdCardDirectory.getAbsolutePath() + File.separator + "LINKS",
                        "Content_Links", "Content_Links");
                if(!contentLinkCopyResult) {
                    LOGGER.error("Content links file no copied");
                    throw new ServletException("Content links file no copied");
                }

                boolean moduleLinkCopyResult = handler.copy(Config.DESKTOP_LOCATION + File.separator +"LINKS",
                        sdCardDirectory.getAbsolutePath() + File.separator + "LINKS",
                        "Module_Links", "Module_Links");
                if(!moduleLinkCopyResult) {
                    LOGGER.error("Module links file no copied");
                    throw new ServletException("Module links file no copied");
                }

                // writing metadata
                File verificationFile = new File(sdCardDirectory.getAbsolutePath(),
                        V_VERIFICATION_FILE);
                JSONFileWriter verificationFileWriter = new JSONFileWriter(verificationFile);

                SDCardGroupMetadata groupMetadata = new SDCardGroupMetadata(group.targetId,
                        group.targetType, group.cardSize, group.noOfCards, group.cardIds,
                        organization.id, group.name, group.id, group.size);

                // writing .vg file
                File groupFile = new File(sdCardDirectory.getAbsolutePath(), V_SDCARD_GROUP_FILE);
                JSONFileWriter groupWriter = new JSONFileWriter(groupFile, organization.key);
                groupWriter.write(groupMetadata.toJSON().toString(), false);
                groupWriter.close();

                String checkSum = FileUtils.checksumMD5(groupFile);
                LOGGER.debug(" Checksum for " + V_SDCARD_GROUP_FILE + " " + checkSum);
                SDCardChecksum checkSumData = new SDCardChecksum(V_SDCARD_GROUP_FILE, checkSum);
                verificationFileWriter.write(checkSumData.toJSON().toString(), true);

                // writing .vs file
                File sdcardFile = new File(sdCardDirectory.getAbsolutePath(), V_SDCARD_FILE);
                SDCardMetadata cardMetadata = new SDCardMetadata(card.name, card.id, card.size,
                        card.contentSize);
                JSONFileWriter vsFileWriter = new JSONFileWriter(sdcardFile, organization.key);
                vsFileWriter.write(cardMetadata.toJSON().toString(), true);
                vsFileWriter.close();

                checkSum = FileUtils.checksumMD5(sdcardFile);
                LOGGER.debug(" Checksum for " + V_SDCARD_FILE + " " + checkSum);

                checkSumData = new SDCardChecksum(V_SDCARD_FILE, checkSum);
                verificationFileWriter.write(checkSumData.toJSON().toString(), true);
                // writing

                checkSum = FileUtils.checksumMD5(contentMetadataFile);
                LOGGER.debug(" Checksum for " + V_FILE_METADATA_FILE + " " + checkSum);
                checkSumData = new SDCardChecksum(V_FILE_METADATA_FILE, checkSum);
                verificationFileWriter.write(checkSumData.toJSON().toString(), true);

                /*
                  Collecting all file information and checking
                  che/home/vedantu/temp/fs/flashed/ncert/card-1/vedantuck sum after excluding .vv
                 */

                StringBuilder stringBuilder = new StringBuilder();
                List<String> fileList = new ArrayList<String>();
                FileUtils.listf(sdCardDirectory, fileList, ".");
                for (String fileName : fileList) {
                    if (!fileName.contains("./" + V_VERIFICATION_FILE)) {
                        LOGGER.debug("FileName " + fileName);
                        stringBuilder.append(fileName);
                    }
                }

                ByteArrayInputStream stream = new ByteArrayInputStream(stringBuilder.toString().getBytes());
                checkSum = FileUtils.checksumMD5(stream);
                LOGGER.debug(" Checksum for " + "contents" + " " + checkSum);
                checkSumData = new SDCardChecksum(".contents", checkSum);
                verificationFileWriter.write(checkSumData.toJSON().toString(), true);
                verificationFileWriter.close();
                stream.close();
                FlashRecordDataManager.INSTANCE.increment(recordInfo, 1);
                LOGGER.debug(" Updating job to completed now... flashed now  ");

                job.status = JobState.COMPLETED.name();

                JobInfoDataManager.INSTANCE.update(job, Job.FIELD_STATUS);

                return;
            } catch (IOException e) {
                LOGGER.error(" IO Exception occured", e);
            }

        } catch (Exception e) {
            LOGGER.error("Unabled to package sd card so marking job as failed", e);
            job.status = JobState.FAILED.name();
            JobInfoDataManager.INSTANCE.update(job, Job.FIELD_STATUS);
        }
    }

}
