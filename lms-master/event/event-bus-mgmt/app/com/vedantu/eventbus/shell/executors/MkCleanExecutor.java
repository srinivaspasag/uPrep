package com.vedantu.eventbus.shell.executors;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.Scanner;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.utils.OptionValue;
import com.vedantu.commons.utils.ShellExecutor;

public class MkCleanExecutor extends ShellExecutor {

    private static final ALogger LOGGER = Logger.of(MkCleanExecutor.class);

    public MkCleanExecutor() {

        super("mkclean");
    }

    public boolean convert(File inputVideo, File outputFileVideo) throws VedantuException {

        if (!inputVideo.exists()) {
            LOGGER.error("File doesn't exist  :" + inputVideo.getAbsolutePath());
            throw new VedantuException(VedantuErrorCode.FILE_NOT_FOUND, "File doesn't exist  :"
                    + inputVideo.getAbsolutePath());
        }

        if (outputFileVideo.exists()) {
            LOGGER.error("File exist  :" + outputFileVideo.getAbsolutePath() + " so removing file ");
            outputFileVideo.delete();
        }

        OptionValue keepCuesOption = new OptionValue();
        keepCuesOption.option = "--keep-cues";

        this.options.add(0, keepCuesOption);


        OptionValue inputOption = new OptionValue();
        inputOption.value = inputVideo.getAbsolutePath();
        this.options.add(inputOption);


        OptionValue outputFileOption = new OptionValue();
        outputFileOption.value = outputFileVideo.getAbsolutePath();
        this.options.add(outputFileOption);

        this.execute();

        LOGGER.debug("Can not convert file");

        if (this.executionStream != null) {

            LOGGER.error("Execution log start: ");
            Scanner s = new Scanner(new BufferedInputStream(this.executionStream));
            while (s.hasNextLine()) {

                String nextLine = s.nextLine().trim();
                LOGGER.error("Execution stream :" + nextLine);

            }

            LOGGER.debug("Execution log end ");
        }

        if (this.errorStream != null) {
            LOGGER.error("Error Log start ");
            boolean flagged = false;
            try {

                LOGGER.error(" Non tempty error stream so error might have occured");
                Scanner s = new Scanner(new BufferedInputStream(this.errorStream));
                while (s.hasNextLine()) {
                    String nextLine = s.nextLine();
                    LOGGER.error(" ErrorStream" + nextLine);
                    flagged = true;
                }

            } catch (Exception e) {
                LOGGER.error(" Error while converting video occured ", e);
                flagged = true;
            } finally {
                if (flagged) {
                    throw new VedantuException(VedantuErrorCode.CONVERSION_FAILED,
                            "Conversion for video file " + inputVideo.getAbsolutePath() + " failed");
                }
            }

        }
        LOGGER.error("Error Log end");

        if (getExitValue() != 0) {
            throw new VedantuException(VedantuErrorCode.CONVERSION_FAILED,
                    "Conversion for video file " + inputVideo.getAbsolutePath() + " failed");
        }

        return true;

    }

}
