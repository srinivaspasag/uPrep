package com.lms.common.utils;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.Scanner;

@Setter
@Getter
public class CommandLineUtils {
    private static final Logger logger = LoggerFactory.getLogger(CommandLineUtils.class);

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
                logger.debug("value " + nextLine);

                buffer.append(nextLine);
                break;
            }
            return buffer.toString();
        } catch (Exception e) {
            logger.error("Error" + e);
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

    }
}
