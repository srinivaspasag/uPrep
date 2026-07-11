package com.vedantu.commons.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.Scanner;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;

public class CommandLineUtils {

    private static final ALogger LOGGER = Logger.of(CommandLineUtils.class);

    public static String getCurrentDeployedVersion(String deploymentDirectory)
            throws VedantuException {

        Process process = null;
        Scanner scanner = null;
        try {
            process = Runtime.getRuntime().exec("git describe --all", null,
                    new File(deploymentDirectory));
            final int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("program didnt exit with 0, but with " + exitCode);
            }

            scanner = new Scanner(new BufferedInputStream(process.getInputStream()));
            StringBuffer buffer = new StringBuffer();
            while (scanner.hasNextLine()) {
                String nextLine = scanner.nextLine();
                LOGGER.debug("value " + nextLine);
                
                buffer.append(nextLine);
                break;
            }
            return buffer.toString();
        } catch (Exception e) {
            LOGGER.error("Error" + e);
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

    }
}
