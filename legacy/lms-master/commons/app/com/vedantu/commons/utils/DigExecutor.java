package com.vedantu.commons.utils;

import java.io.BufferedInputStream;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuException;

public class DigExecutor extends ShellExecutor {

    String                       keyValuePattern = "\\w+=.*";

    private static final ALogger LOGGER          = Logger.of(DigExecutor.class);

    private String               matchCNAME      = null;

    boolean                      CNAMEFound      = false;

    public DigExecutor() {

        super("dig");
        monitorable = false;
    }

    public boolean match(String localSetup) throws VedantuException {
        OptionValue value = new OptionValue();
        value.option=StringUtils.EMPTY;
        value.delimeter=StringUtils.EMPTY;
        value.value=matchCNAME;
        this.options.add(value);
        this.execute();
        this.parseExecutionStream( localSetup);
        return CNAMEFound;

    }

    public void setMatchCNAME(String matchCNAME) {

        this.matchCNAME = matchCNAME;
    }

    private void parseExecutionStream(String referer) {

        if (this.executionStream != null) {
            LOGGER.debug("Execution log start: ");
            Scanner s = new Scanner(new BufferedInputStream(this.executionStream));
            while (s.hasNextLine()) {

                String nextLine = s.nextLine().trim();
                LOGGER.debug(nextLine);
                if (nextLine.contains("CNAME") && nextLine.contains(referer)) {
                    CNAMEFound = true;
                }
            }

            LOGGER.debug("Execution log end ");
        }
    }

}