package com.vedantu.eventbus.processors.document;

import java.io.File;
import java.net.ConnectException;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;

public class PDFConvertor {

    private static ALogger      LOGGER                         = Logger.of(PDFConvertor.class);
    private static String       OPEN_OFFICE_PORT_CONFIGURATION = "open.office.port";
    private final static int    DEFAULT_OPEN_OFFICE_PORT       = 8100;
    private static PDFConvertor instance;
    private static int          openOfficePort                 = DEFAULT_OPEN_OFFICE_PORT;

    private PDFConvertor() {

        loadProperties();
    }

    // singleton pattern
    public static PDFConvertor getInstance() {

        if (instance == null) {
            createInstance();
        }
        return instance;
    }

    /**
     * This function needs openoffice service to be started on specified port You can install
     * service using following command unless openoffice is installed on machine
     * /usr/lib/libreoffice/program/soffice.bin --headless --nofirststartwizard
     * --accept='socket,host=localhost,port=8100;urp;StartOffice.Service'
     * 
     * 
     * @param input
     * @param pdfOutput
     * @return
     */

    public boolean convertToPdf(File input, File pdfOutput) {

        // connect to an OpenOffice.org instance running on port defined by
        // property file

        try {
            OpenOfficeConnection connection = new SocketOpenOfficeConnection(openOfficePort);
            LOGGER.trace("converting file to PDF using OpenOffice");
            if (!connection.isConnected()) {
                connection.connect();
            }

            DocumentConverter convertor = new OpenOfficeDocumentConverter(connection);
            convertor.convert(input, pdfOutput);
            connection.disconnect();
            LOGGER.trace("Conversion of file " + input.getAbsolutePath()
                    + " is successful generating file " + pdfOutput);

        } catch (ConnectException e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
        return true;

    }

    private static synchronized void createInstance() {

        if (instance == null) {
            instance = new PDFConvertor();
        }
    }

    private void loadProperties() {

        try {
            openOfficePort = Play.application().configuration()
                    .getInt(OPEN_OFFICE_PORT_CONFIGURATION, DEFAULT_OPEN_OFFICE_PORT);

        } catch (Exception e) {
            LOGGER.error("can not read the openoffice.port value from application.conf file ", e);
        }
    }
}
