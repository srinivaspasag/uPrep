package com.vedantu.web.datacollector;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;

public abstract class AbstractVideoDataCollector implements IDataCollector {
	private static final ALogger LOGGER = Logger
			.of(AbstractVideoDataCollector.class);

	@Override
	public VideoInfo getData(String url) {
		Source source = null;
		try {
			source = new Source(new URL(url));
		} catch (MalformedURLException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

		if (source == null) {
			return null;
		}
		Map<String, String> openGraphKeysMap = new HashMap<String, String>();
		List<Element> c = source.getAllElements("meta");
		if (c != null && !c.isEmpty()) {
			for (Element e : c) {
				LOGGER.debug(e.getAttributeValue("property") + " "
						+ e.getAttributes());
				String keywords = e.getAttributeValue("name");
				String openGraphKey = e.getAttributeValue("property");
				if (StringUtils.isNotEmpty(openGraphKey)) {
					openGraphKeysMap.put(openGraphKey,
							e.getAttributeValue("content"));
				}
				if (StringUtils.isNotEmpty(keywords)
						&& StringUtils.equalsIgnoreCase(keywords, "keywords")) {
					openGraphKeysMap.put(keywords,
							e.getAttributeValue("content"));
				}

			}
		}
		VideoInfo videoInfo = getVideoInfo(openGraphKeysMap);
		LOGGER.debug("final open graphMap is : " + openGraphKeysMap);
		return videoInfo;
	}

	public VideoInfo getVideoInfo(Map<String, String> openGraphKeysMap) {
		VideoInfo videoInfo = new VideoInfo();
		videoInfo.description = openGraphKeysMap.get("og:description");
		videoInfo.duration = StringUtils.isNotEmpty(openGraphKeysMap
				.get("video:duration")) ? Integer.parseInt(openGraphKeysMap
				.get("video:duration")) : 0;
		videoInfo.image = openGraphKeysMap.get("og:image");
		videoInfo.site_name = openGraphKeysMap.get("og:site_name");
		videoInfo.tags = StringUtils.isNotEmpty(openGraphKeysMap
				.get("keywords")) ? new HashSet<String>(
				Arrays.asList(StringUtils.split(
						openGraphKeysMap.get("keywords"), ",")))
				: new HashSet<String>();
		videoInfo.title = openGraphKeysMap.get("og:title");
		videoInfo.url = openGraphKeysMap.get("og:url");
		videoInfo.video = openGraphKeysMap.get("og:video");
		return videoInfo;
	}

	public abstract String getVideoId(String url);

	public abstract String getJSONAPIUrl(String videoId);

	public abstract VideoInfo getVideoInfo(JSONObject json);
	
	
	

}
