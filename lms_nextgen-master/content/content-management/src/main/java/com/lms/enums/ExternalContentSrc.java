package com.lms.enums;

import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.interfaces.IDataCollector;
import com.lms.web.OpenGraphVideoDataCollector;
import com.lms.web.VimeoDataCollector;
import com.lms.web.YouTubeDataCollector;
import org.springframework.util.StringUtils;

public enum ExternalContentSrc {

    Vimeo(new VimeoDataCollector(), "vimeo.com"), YouTube(
            new YouTubeDataCollector(), "youtube.com"), UNKNOWN(
            new OpenGraphVideoDataCollector());

    private final IDataCollector dataCollector;
    private final String urlDomain;

    ExternalContentSrc(IDataCollector dataCollector, String urlDomain) {
        this.dataCollector = dataCollector;
        this.urlDomain = urlDomain;
    }

    ExternalContentSrc(IDataCollector dataCollector) {
        this(dataCollector, HardCodedConstants.emptyString);
    }

    public String getUrlDomain() {
        return urlDomain;
    }

    public static ExternalContentSrc getSrc(String url) {
        ExternalContentSrc contentSource = null;
        if (StringUtils.isEmpty(url)) {
            return contentSource;
        }
        for (ExternalContentSrc src : ExternalContentSrc.values()) {
            if (url.contains(src.urlDomain)) {
                contentSource = src;
                break;
            }
        }
        if (contentSource == null) {
            contentSource = UNKNOWN;
        }
        return contentSource;
    }

    public IDataCollector getDataCollector() {
        return dataCollector;
    }

}