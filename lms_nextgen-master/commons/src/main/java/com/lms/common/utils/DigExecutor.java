package com.lms.common.utils;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.constants.HardCodedConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.util.Scanner;

public class DigExecutor extends ShellExecutor {

    String                       keyValuePattern = "\\w+=.*";


    private static final Logger logger = LoggerFactory.getLogger(DigExecutor.class);

    private String               matchCNAME      = null;

    boolean                      CNAMEFound      = false;

    public DigExecutor() {

        super("dig");
        monitorable = false;
    }


    public boolean match(String localSetup) throws VedantuException {
        OptionValue value = new OptionValue();
        value.option= HardCodedConstants.emptyString;
        value.delimeter=HardCodedConstants.emptyString;
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
            logger.debug("Execution log start: ");
            Scanner s = new Scanner(new BufferedInputStream(this.executionStream));
            while (s.hasNextLine()) {

                String nextLine = s.nextLine().trim();
                logger.debug(nextLine);
                if (nextLine.contains("CNAME") && nextLine.contains(referer)) {
                    CNAMEFound = true;
                }
            }

            logger.debug("Execution log end ");
        }
    }

}