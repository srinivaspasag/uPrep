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

public class PDFThumbnailGenerator extends ShellExecutor {

    private static final String GS_CMD_PARAM_D_LAST_PAGE = "-dLastPage";
    private static final String GS_CMD_PARAM_S_DEVICE    = "-sDEVICE";
    private static final String GS_CMDS_PARAM_O          = "-o";
    private static final String GS_CMD_PARAM_Q           = "-q";
    private static ALogger      LOGGER                   = Logger.of(PDFThumbnailGenerator.class);

    public PDFThumbnailGenerator() {

        super("gs");

    }

    // gs -q -o uuid.jpeg -sDEVICE=jpeg -dLastPage=1 test.pdf

    public boolean convert(File inputPDF, File outputThumbnail) throws VedantuException {

        if (!inputPDF.exists()) {
            LOGGER.error("File doesn't exist  :" + inputPDF.getAbsolutePath());
            throw new VedantuException(VedantuErrorCode.FILE_NOT_FOUND, "File doesn't exist  :"
                    + inputPDF.getAbsolutePath());
        }
        if (outputThumbnail.exists()) {
            LOGGER.error("File exist  :" + outputThumbnail.getAbsolutePath() + " so removing file ");
            outputThumbnail.delete();

        }
        OptionValue commandLineOption = new OptionValue();
        commandLineOption.option = GS_CMD_PARAM_Q;
        this.options.add(commandLineOption);

   
        commandLineOption = new OptionValue();
        commandLineOption.option = GS_CMDS_PARAM_O;
        commandLineOption.value = outputThumbnail.getAbsolutePath();
        this.options.add(commandLineOption);

        commandLineOption = new OptionValue();
        commandLineOption.option = GS_CMD_PARAM_S_DEVICE;
        commandLineOption.value = "jpeg";
        commandLineOption.delimeter = EQAUL; 
        this.options.add(commandLineOption);

        commandLineOption = new OptionValue();
        commandLineOption.option = GS_CMD_PARAM_D_LAST_PAGE;
        commandLineOption.delimeter = EQAUL; 
        commandLineOption.value = "1";
        this.options.add(commandLineOption);

        commandLineOption = new OptionValue();
        commandLineOption.value = inputPDF.getAbsolutePath();
        this.options.add(commandLineOption);

        this.execute();
        if (!outputThumbnail.exists()) {
            LOGGER.debug("Can not generate file");

            if (this.errorStream != null) {
                LOGGER.error("Error Log start ");
                Scanner s = new Scanner(new BufferedInputStream(this.errorStream));
                while (s.hasNextLine()) {

                    LOGGER.error(s.nextLine());
                }

                LOGGER.error("Error Log end");
                return false;
            }

            if (this.executionStream != null) {

                LOGGER.debug("Execution log start: ");
                Scanner s = new Scanner(new BufferedInputStream(this.executionStream));
                while (s.hasNextLine()) {
                    LOGGER.debug(s.nextLine());
                }
                LOGGER.debug("Execution log end ");
            }
            return false;

        }
        return true;

    }

}
