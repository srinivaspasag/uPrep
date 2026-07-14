package com.vedantu.web.enums;

import org.apache.commons.lang3.StringUtils;

import com.vedantu.web.datacollector.IDataCollector;
import com.vedantu.web.datacollector.OpenGraphVideoDataCollector;
import com.vedantu.web.datacollector.VimeoDataCollector;
import com.vedantu.web.datacollector.YouTubeDataCollector;

public enum ExternalContentSrc {

	Vimeo(new VimeoDataCollector(), "vimeo.com"), YouTube(
			new YouTubeDataCollector(), "youtube.com"), UNKNOWN(
			new OpenGraphVideoDataCollector());

	private IDataCollector dataCollector;
	private String urlDomain;

	private ExternalContentSrc(IDataCollector dataCollector, String urlDomain) {
		this.dataCollector = dataCollector;
		this.urlDomain = urlDomain;
	}

	private ExternalContentSrc(IDataCollector dataCollector) {
		this(dataCollector, StringUtils.EMPTY);
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
