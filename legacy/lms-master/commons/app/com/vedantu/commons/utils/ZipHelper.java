package com.vedantu.commons.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import play.Logger;
import play.Logger.ALogger;

public class ZipHelper {

    private static final ALogger LOGGER = Logger.of(ZipHelper.class);

    public void zipDir(String dirName, String nameZipFile) throws IOException {

        ZipOutputStream zip = null;
        FileOutputStream fW = null;
        fW = new FileOutputStream(nameZipFile);
        zip = new ZipOutputStream(fW);
        File folder = new File(dirName);
        for( String fileName : folder.list()){
            LOGGER.debug( " fileName " + fileName + " currentFolder Name "
                    + folder.getName());
            addFileToZip("",dirName+"/"+ fileName, zip, false);

        }
        zip.close();
        fW.close();
    }

    private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip)
            throws IOException {

        File folder = new File(srcFolder);
        if (folder.list().length == 0) {
            addFileToZip(path, srcFolder, zip, true);
        } else {
            for (String fileName : folder.list()) {
                LOGGER.debug(" Path " + path + " fileName " + fileName + " currentFolder Name "
                        + folder.getName());

                if (path.equals("")) {

                    addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip, false);
                } else {
                    addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip,
                            false);
                }
            }
        }
    }

    private void addFileToZip(String path, String srcFile, ZipOutputStream zip, boolean flag)
            throws IOException {

        File folder = new File(srcFile);
        if (flag) {
            zip.putNextEntry(new ZipEntry(path + "/" + folder.getName() + "/"));
        } else {
            if (folder.isDirectory()) {
                addFolderToZip(path, srcFile, zip);
            } else {
                byte[] buf = new byte[1024];
                int len;
                FileInputStream in = new FileInputStream(srcFile);
                zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
                while ((len = in.read(buf)) > 0) {
                    zip.write(buf, 0, len);
                }
                in.close();
            }
        }
    }
}
