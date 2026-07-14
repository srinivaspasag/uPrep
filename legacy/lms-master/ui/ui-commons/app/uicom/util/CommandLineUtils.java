package uicom.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.Scanner;

import play.Logger;

public class CommandLineUtils {

    public static String getCurrentDeployedVersion(String deploymentDirectory) {

        Process process = null;
        Scanner scanner = null;
        StringBuffer buffer = new StringBuffer();
        try {
            process = Runtime.getRuntime().exec("git describe --all", null,
                    new File(deploymentDirectory));
            final int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("program didnt exit with 0, but with " + exitCode);
            }

            scanner = new Scanner(new BufferedInputStream(process.getInputStream()));

            while (scanner.hasNextLine()) {
                String nextLine = scanner.nextLine();
                Logger.log4j.info("value " + nextLine);

                buffer.append(nextLine);
                break;
            }

        } catch (Exception e) {
            Logger.log4j.error("Error" + e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return buffer.toString();
    }
}
