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

public class QPDFFileLinearizer extends ShellExecutor {

    private static ALogger LOGGER = Logger.of(QPDFFileLinearizer.class);

    public QPDFFileLinearizer() {

        super("qpdf");
        OptionValue linearizeOption = new OptionValue();
        linearizeOption.option="--linearize";
        this.options.add(linearizeOption);

    }

    // qpdf input output
    public boolean convert(File inputPDF, File outputPDF) throws VedantuException {

        if (!inputPDF.exists()) {
            LOGGER.error("File doesn't exist  :" + inputPDF.getAbsolutePath());
            throw new VedantuException(VedantuErrorCode.FILE_NOT_FOUND, "File doesn't exist  :"
                    + inputPDF.getAbsolutePath());
        }

        if (outputPDF.exists()) {
            LOGGER.error("File exist  :" + outputPDF.getAbsolutePath() + " so removing file ");
            outputPDF.delete();

        }
        OptionValue inputFileOption = new OptionValue();
        inputFileOption.value = inputPDF.getAbsolutePath();
        this.options.add(inputFileOption);

        OptionValue outputFileOption = new OptionValue();
        outputFileOption.value = outputPDF.getAbsolutePath();
        this.options.add(outputFileOption);

        this.execute();
        if (!outputPDF.exists()) {
            LOGGER.debug("Can not linearlize file");

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
