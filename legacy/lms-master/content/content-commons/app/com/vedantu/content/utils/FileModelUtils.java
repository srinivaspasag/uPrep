package com.vedantu.content.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

import org.apache.commons.io.IOUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.entity.storage.AbstractEntityFileStorage;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.fs.handlers.FileSystemFactory;
import com.vedantu.commons.fs.handlers.LocalFileSystemHandler;
import com.vedantu.commons.pojos.FileData;
import com.vedantu.content.models.AbstractFileModel;

public class FileModelUtils {

    // TODO put a test for this function
    private final static ALogger LOGGER             = Logger.of(FileModelUtils.class);

    public final static long     MAX_RETRY          = 10;
    public final static long     sleepTimeInSeconds = 60;

    public static File moveFileLocally(AbstractEntityFileStorage storage, String fileName,
            AbstractFileModel contentModel, MediaType fileMediaType) {

        LocalFileSystemHandler tempFS = FileSystemFactory.INSTANCE.getTempFS();
        String testForLocalFilePath = tempFS.getFilePath(storage.getStorageId(), fileName);
        File file = new File(testForLocalFilePath);
        boolean fileWrittenSuccessFully = false;
        FileOutputStream stream = null;

        RandomAccessFile fileToWrite = null;
        FileData data = null;
        FileLock lock = null;
        try {
            data = storage.getFromFs(storage.getStorageId(), fileName);

            // check if file is being copied by two threads at same location
            // TODO might check with local file existence handler for removal or updating life time
            // of this file on disk

            if (!file.exists()) {
                fileToWrite = new RandomAccessFile(file, "rw");
                int retry = 0;
                for (retry = 0; retry < MAX_RETRY; retry++) {

                    if (fileToWrite.getChannel().size() != data.getFileSize()) {
                        LOGGER.debug("Acquiring lock local on file" + file.getAbsolutePath());
                        lock = fileToWrite.getChannel().tryLock();
                        if (lock != null) {
                            fileToWrite.getChannel().truncate(0);
                            LOGGER.debug("Acquired local lock on file" + file.getAbsolutePath());
                            break;
                        }
                    }
                    Thread.sleep(sleepTimeInSeconds * 1000);
                }
                if (retry == MAX_RETRY) {
                    return null;
                }
                if (lock != null) {

                    stream = new FileOutputStream(fileToWrite.getFD());
                    IOUtils.copy(data.getIn(), stream);
                    IOUtils.closeQuietly(data.getIn());
                    IOUtils.closeQuietly(stream);
                    fileWrittenSuccessFully = true;
                }
            }

            return file;
        } catch (IOException e) {
            LOGGER.error("can not move file from  storage :  " + storage.getStorageId() + " to "
                    + file.getAbsolutePath(), e);

            return null;
        } catch (EntityFileStorageException e) {
            LOGGER.error("can not move file from  storage :  " + storage.getStorageId() + " to "
                    + file.getAbsolutePath(), e);

            return null;
        } catch (InterruptedException e) {
            LOGGER.error("can not move file from  storage :  " + storage.getStorageId() + " to "
                    + file.getAbsolutePath(), e);

            return null;
        } finally {

            if (data != null) {
                IOUtils.closeQuietly(data.getIn());
            }
            IOUtils.closeQuietly(stream);
            IOUtils.closeQuietly(fileToWrite);

        }

    }
}
