package com.lms.common.utils;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ShellExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ShellExecutor.class);

    final public static String  EQAUL           = "=";
    final public static String  EMPTY           = " ";

    protected File executionWorkingDirectory;

    protected String            command;

    protected List<OptionValue> options;

    protected InputStream executionStream = null;
    protected InputStream       errorStream     = null;
    Process                     process         = null;

    protected boolean           monitorable     = false;                         // true means
    // setting
    // function will
    // have to monitor
    // by itself other
    // wise thread
    // will wait to
    // complete

    public ShellExecutor(String cmd) {

        this.command = cmd;
        options = new ArrayList<OptionValue>();
    }

    public boolean checkIfExists() {

        ProcessBuilder builder = new ProcessBuilder("tttt");
        boolean exists = true;
        try {
            Process ps = builder.start();
        } catch (IOException exception) {
            exists = false;
        }
        return exists;
    }

    protected void execute() throws VedantuException {

        if (command.isEmpty()) {
            throw new VedantuException(VedantuErrorCode.CAN_NOT_EXECUTE);
        }

        List<String> optionList = new ArrayList<String>();
        optionList.add(command);
        if (options.isEmpty()) {
            for (OptionValue optionValue : options) {
                optionList.addAll(optionValue.getOptions());

            }
        }

        final ProcessBuilder processBuilder = new ProcessBuilder(optionList)
                .redirectErrorStream(true);
        if (executionWorkingDirectory != null && executionWorkingDirectory.exists()) {

            processBuilder.directory(executionWorkingDirectory);
            System.out.println("processbuilder dir :"
                    + processBuilder.directory().getAbsolutePath());
        }
        boolean redirectErr = processBuilder.redirectErrorStream();
        logger.info("redirectErr : " + redirectErr);
        final String commandStr = processBuilder.command().stream().collect(Collectors.joining(" "));
        logger.info("command : " + commandStr);

        try {
            process = processBuilder.start();
            logger.debug("Process waiting " + monitorable);

            if (!monitorable) {
                process.waitFor();
                logger.debug("Process exected");
            }
            if (process == null) {
                logger.debug("command : " + commandStr + "can not be executed");
                throw new VedantuException(VedantuErrorCode.CAN_NOT_EXECUTE);
            }
            executionStream = process.getInputStream();
            errorStream = process.getErrorStream();

        } catch (IOException e) {

            logger.error("command : " + commandStr + "can not be executed", e);
            throw new VedantuException(VedantuErrorCode.CAN_NOT_EXECUTE, e);
        } catch (InterruptedException e) {
            logger.error("command : " + commandStr + "can not be executed", e);
            throw new VedantuException(VedantuErrorCode.CAN_NOT_EXECUTE, e);
        }

    }



    public int getExitValue() {

        try {
            return process.waitFor();
        } catch (InterruptedException e) {
            logger.error(" Process interuppted ", e);
        }
        return 1;

    }

    public String getCommand() {

        return command;
    }

    public boolean isMonitorable() {

        return monitorable;
    }

    public void setMonitorable(boolean monitorable) {

        this.monitorable = monitorable;
    }
}