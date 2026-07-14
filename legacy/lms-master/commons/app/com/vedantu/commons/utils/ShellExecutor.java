package com.vedantu.commons.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;

public abstract class ShellExecutor {

    private static ALogger      LOGGER          = Logger.of(ShellExecutor.class);

    final public static String  EQAUL           = "=";
    final public static String  EMPTY           = " ";

    protected File              executionWorkingDirectory;

    protected String            command;

    protected List<OptionValue> options;

    protected InputStream       executionStream = null;
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

        if (StringUtils.isEmpty(command)) {
            throw new VedantuException(VedantuErrorCode.CAN_NOT_EXECUTE);
        }

        List<String> optionList = new ArrayList<String>();
        optionList.add(command);
        if (CollectionUtils.isNotEmpty(options)) {
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
        LOGGER.info("redirectErr : " + redirectErr);
        final String commandStr = StringUtils.join(processBuilder.command(), " ");
        LOGGER.info("command : " + commandStr);

        try {
            process = processBuilder.start();
            LOGGER.debug("Process waiting " + monitorable);

            if (!monitorable) {
                process.waitFor();
                LOGGER.debug("Process exected");
            }
            if (process == null) {
                LOGGER.debug("command : " + commandStr + "can not be executed");
                throw new VedantuException(VedantuErrorCode.CAN_NOT_EXECUTE);
            }
            executionStream = process.getInputStream();
            errorStream = process.getErrorStream();

        } catch (IOException e) {

            LOGGER.error("command : " + commandStr + "can not be executed", e);
            throw new VedantuException(VedantuErrorCode.CAN_NOT_EXECUTE, e);
        } catch (InterruptedException e) {
            LOGGER.error("command : " + commandStr + "can not be executed", e);
            throw new VedantuException(VedantuErrorCode.CAN_NOT_EXECUTE, e);
        }

    }

    @Override
    public void finalize() throws Throwable {

        IOUtils.closeQuietly(executionStream);
        IOUtils.closeQuietly(errorStream);

        super.finalize();

    }

    public int getExitValue() {

        try {
            return process.waitFor();
        } catch (InterruptedException e) {
            LOGGER.error(" Process interuppted ", e);
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
