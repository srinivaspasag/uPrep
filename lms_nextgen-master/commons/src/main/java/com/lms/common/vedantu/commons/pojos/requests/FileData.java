package com.lms.common.vedantu.commons.pojos.requests;

import com.lms.common.vedantu.entity.storage.AbstractEntityFileStorage;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FileData {

    public static final String CONTENT_TYPE       = "Content-Type";
    public Map<String, String> fileMetaInfo       = null;
    private InputStream inputStream        = null;
    private long               readContentLength  = AbstractEntityFileStorage.MAXIMUM_FILE_SIZE_ALLOWED;
    private long               totalContentLength = -1;
    private long               fileSize;

    private String             securedURL;

    public FileData() {

        fileMetaInfo = new HashMap<String, String>();
    }

    public FileData(Map<String, String> fileMetaInfo, InputStream in) {

        super();
        this.fileMetaInfo = fileMetaInfo == null ? new HashMap<String, String>() : fileMetaInfo;
        this.inputStream = in;
    }

    public long getFileSize() {

        return fileSize;
    }

    public void setFileSize(long fileSize) {

        this.fileSize = fileSize;
    }

    // public long getTotalContentLength() {
    //
    // return totalContentLength;
    // }
    //
    // public void setTotalContentLength(long totalContentLength) {
    //
    // this.totalContentLength = totalContentLength;
    // }

    public long getContentLength() {

        return readContentLength;
    }

    public void setContentLength(long contentLength) {

        this.readContentLength = contentLength;
    }

    public Map<String, String> getFileMetaInfo() {

        return fileMetaInfo;
    }

    public InputStream getIn() {

        return inputStream;
    }

    public void setFileMetaInfo(Map<String, String> fileMetaInfo) {

        this.fileMetaInfo = fileMetaInfo;
    }

    public void setIn(InputStream in) {

        this.inputStream = in;
    }

    public String getSecuredURL() {

        return securedURL;
    }

    public void setSecuredURL(String securedURL) {

        this.securedURL = securedURL;

    }

    public String toString() {

        StringBuffer buff = new StringBuffer();
        buff = buff.append("FileSize:").append(fileSize).append("Metadata")
                .append(fileMetaInfo);
        return buff.toString();
    }
}
