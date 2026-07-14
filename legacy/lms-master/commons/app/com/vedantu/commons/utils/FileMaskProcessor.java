package com.vedantu.commons.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import play.Logger;
import play.Logger.ALogger;

public class FileMaskProcessor {

    public static final ALogger LOGGER = Logger.of(FileMaskProcessor.class);

    private final String        passPhrase;
    private final int           passBytesSize;
    private final byte[]        passPhraseBytes;

    public FileMaskProcessor(final String passPhrase, final int passBytesSize) {

        this.passPhrase = passPhrase;
        this.passBytesSize = passBytesSize;
        this.passPhraseBytes = passPhraseToByteArray();
    }

    private byte[] passPhraseToByteArray() {

        byte[] t = passPhrase.getBytes();
        byte[] p = Arrays.copyOfRange(t, 0, passBytesSize);
        return p;
    }

    public boolean process(File inFile, int readBuffderSize, File outFile) {

        long start = System.currentTimeMillis();
        FileOutputStream fos = null;
        FileInputStream fis = null;

        try {
            byte[] readBuffer = new byte[readBuffderSize];
            File locationDir = new File(outFile.getParent());
            if (!locationDir.canWrite()) {
                LOGGER.error("Can write to location" + outFile.getAbsolutePath());
                return false;
            }
            fos = new FileOutputStream(outFile);
            fis = new FileInputStream(inFile);
            int bytesRead = 0;
            int totalBytesRead = 0;
            while ((bytesRead = fis.read(readBuffer)) != -1) {
                byte[] writeBuffer = new byte[bytesRead];
                process(readBuffer, totalBytesRead, bytesRead, writeBuffer);
                fos.write(writeBuffer);
                totalBytesRead = totalBytesRead + bytesRead;
            }
            fis.close();
            fos.close();

        } catch (Exception e) {
            LOGGER.error("Can not encrypt file " + inFile.getAbsolutePath() + "to create file "
                    + outFile.getAbsolutePath(), e);
            return false;
        } finally {
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(fos);

        }
        long end = System.currentTimeMillis();
        long timeTaken = end - start;

        LOGGER.debug("time taken: " + timeTaken + "ms");
        return true;
    }

    public String process(String inString) {

        byte[] outBytes = new byte[inString.length()];
        process(inString.getBytes(), 0, outBytes.length, outBytes);
        return new String(outBytes);
    }

    public void process(byte[] bytes, int startPoint, int length, byte[] result) {

        byte[] t = Arrays.copyOfRange(bytes, 0, length);
        byte[] p = getPassPhrase(startPoint, length);
        for (int i = 0; i < length; i++) {
            result[i] = (byte) (t[i] ^ p[i]);
        }
    }

    private byte[] getPassPhrase(int startPoint, int length) {

        int remainingFromLastBoundary = startPoint % passBytesSize;
        int tempLength = remainingFromLastBoundary + length;
        if ((tempLength % passBytesSize) != 0) {
            tempLength = ((tempLength / passBytesSize) + 1) * passBytesSize;
        }

        byte[] tempPassPhrase = new byte[tempLength];
        for (int i = 0, j = 0; i < tempLength; i++, j = (j + 1) % passBytesSize) {
            tempPassPhrase[i] = passPhraseBytes[j];
        }

        byte[] resultPassPhrase = Arrays.copyOfRange(tempPassPhrase, remainingFromLastBoundary,
                remainingFromLastBoundary + length);
        return resultPassPhrase;
    }

}
