package com.vedantu.commons.http;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.constants.HttpConstants;

public class HTTPHeaderFormatter extends HashMap<String, String> {

    private static final ALogger         LOGGER    = Logger.of(HTTPHeaderFormatter.class);
    public final static SimpleDateFormat formatter = new SimpleDateFormat(
                                                           "EEE, dd MMM yyyy HH:mm:ss zzz");

    /**
     * Add Accept ranges header // extend to accept differnt type of data
     * 
     * @return
     */
    public String addAcceptRangesHeader() {

        return this.put(HttpConstants.HTTP_RESPONSE_HEADER_ACCEPT_RANGES, "bytes");
    }

    public String addContentRangeHeader(long offset, long size, long totalContentSize) {

        LOGGER.debug("offset " + offset + "  size " + size + " totalContentSize "
                + totalContentSize);
        if ((offset + size) >= totalContentSize) {
            size = totalContentSize - offset;
        }
        LOGGER.debug("offset " + offset + "  size " + size + " totalContentSize "
                + totalContentSize);
        StringBuilder rangeBuilder = new StringBuilder();
        rangeBuilder.append("bytes").append(" ").append(offset).append("-")
                .append(offset + (size > 0 ? size - 1 : 0)).append("/").append(totalContentSize);
        LOGGER.debug("Content-Range " + rangeBuilder.toString());
        return rangeBuilder.toString();

    }

    public String addContentLengthHeader(long contentLength) {

        return this.put(HttpConstants.HTTP_RESPONSE_HEADER_CONTENT_LENGTH,
                Long.toString(contentLength));

    }

    public String addDateHeader() {

        return this.put(HttpConstants.HTTP_RESPONSE_HEADER_CONTENT_DATE, (new Date()).toString());

    }

}