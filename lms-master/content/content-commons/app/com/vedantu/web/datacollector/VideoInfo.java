package com.vedantu.web.datacollector;

import java.util.Set;

public class VideoInfo extends ExternalContentInfo {

	public String title;
	public String videoId;
	public String description;
	public String image;
	public String url;
	public String video;
	public int duration;
	public String site_name;
	public Set<String> tags;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{title:");
		builder.append(title);
		builder.append(", videoId:");
		builder.append(videoId);
		builder.append(", description:");
		builder.append(description);
		builder.append(", image:");
		builder.append(image);
		builder.append(", url:");
		builder.append(url);
		builder.append(", video:");
		builder.append(video);
		builder.append(", duration:");
		builder.append(duration);
		builder.append(", site_name:");
		builder.append(site_name);
		builder.append(", tags:");
		builder.append(tags);
		builder.append("}");
		return builder.toString();
	}

}
