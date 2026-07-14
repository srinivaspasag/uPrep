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

public class QPDFLinearizationChecker extends ShellExecutor {

    private static final String IS_LINEARIZED = "is linearized";
    private static ALogger      LOGGER        = Logger.of(QPDFLinearizationChecker.class);

    public QPDFLinearizationChecker() {

        super("qpdf");
        OptionValue linearizeOption = new OptionValue();
        linearizeOption.option = "--check";
        this.options.add(linearizeOption);
        this.monitorable = true;

    }

    // qpdf input output
    public boolean check(File inputPDF) throws VedantuException {

        if (!inputPDF.exists()) {
            LOGGER.error("File doesn't exist  :" + inputPDF.getAbsolutePath());
            throw new VedantuException(VedantuErrorCode.FILE_NOT_FOUND, "File doesn't exist  :"
                    + inputPDF.getAbsolutePath());
        }

        OptionValue inputFileOption = new OptionValue();
        inputFileOption.value = inputPDF.getAbsolutePath();
        this.options.add(inputFileOption);

        this.execute();
        LOGGER.debug("checking for error stream");
        if (this.errorStream != null) {
            LOGGER.error("Error Log start ");
            Scanner s = new Scanner(new BufferedInputStream(this.errorStream));
            while (s.hasNextLine()) {

                LOGGER.error(s.nextLine());

            }

            LOGGER.error("Error Log end");

        }
        LOGGER.debug("checking for execution stream");

        if (this.executionStream != null) {

            LOGGER.debug("Execution log start: ");
            Scanner s = new Scanner(new BufferedInputStream(this.executionStream));
            while (s.hasNextLine()) {

                String output = s.nextLine();

                LOGGER.error(output);

                if (output.contains(IS_LINEARIZED)) {
                    return true;
                }
            }
            LOGGER.debug("Execution log end ");
        }

        return false;

    }

}
