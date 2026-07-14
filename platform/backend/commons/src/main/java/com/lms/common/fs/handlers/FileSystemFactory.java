package com.lms.common.fs.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSystemFactory {
    private static final Logger logger = LoggerFactory.getLogger(FileSystemFactory.class);
    public static final FileSystemFactory INSTANCE = new FileSystemFactory();
    private final LocalFileSystemHandler tempFs = new LocalFileSystemHandler();

    public LocalFileSystemHandler getTempFS() {

        return tempFs;
    }
   /* private final IFileSystemHandler      fs;
    private final LocalFileSystemHandler  localFs;
    private final LocalFileSystemHandler  tempFs;
    @Value("${fs.class}")
    private String fsClass;
    @Value("${util.temp_dir}")
    private String utilTempDir;*/

 /*   public FileSystemFactory() {

        String fsToUse = fsClass;
        IFileSystemHandler fs = null;
        try {
            Class<?> clazz = Class.forName("com.lms.common.fs.handlers.LocalFileSystemHandler");
            fs = (IFileSystemHandler) clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            logger.error("ClassNotFound", e);
        } catch (InstantiationException e) {
            logger.error("Can not instantiate ", e);
        } catch (IllegalAccessException e) {
            logger.error("Illegal access", e);
        } catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        this.fs = fs;
       this.localFs = fsToUse.equals( LocalFileSystemHandler.class.getName()) ? (LocalFileSystemHandler) fs
                : new LocalFileSystemHandler();
        this.tempFs = new LocalFileSystemHandler(false,utilTempDir);
    }

    public IFileSystemHandler getFS() {

        return fs;
    }

    public LocalFileSystemHandler getLocalFS() {

        return localFs;
    }

    public LocalFileSystemHandler getTempFS() {

        return tempFs;
    }*/
}
