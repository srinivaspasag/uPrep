package com.vedantu.commons.fs.handlers;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

public class FileSystemFactory {

    private static final ALogger          LOGGER   = Logger.of(FileSystemFactory.class);
    public static final FileSystemFactory INSTANCE = new FileSystemFactory();

    private final IFileSystemHandler      fs;
    private final LocalFileSystemHandler  localFs;
    private final LocalFileSystemHandler  tempFs;

    private FileSystemFactory() {

        String fsToUse = Play.application().configuration().getString("fs.class");
        IFileSystemHandler fs = null;
        try {
            Class<?> clazz = Class.forName(fsToUse);
            fs = (IFileSystemHandler) clazz.newInstance();
        } catch (ClassNotFoundException e) {
            LOGGER.error("ClassNotFound", e);
        } catch (InstantiationException e) {
            LOGGER.error("Can not instantiate ", e);
        } catch (IllegalAccessException e) {
            LOGGER.error("Illegal access", e);
        }
        this.fs = fs;
        this.localFs = StringUtils.equals(fsToUse, LocalFileSystemHandler.class.getName()) ? (LocalFileSystemHandler) fs
                : new LocalFileSystemHandler();
        this.tempFs = new LocalFileSystemHandler(false, Play.application().configuration()
                .getString("util.temp_dir"));
    }

    public IFileSystemHandler getFS() {

        return fs;
    }

    public LocalFileSystemHandler getLocalFS() {

        return localFs;
    }

    public LocalFileSystemHandler getTempFS() {

        return tempFs;
    }

}
