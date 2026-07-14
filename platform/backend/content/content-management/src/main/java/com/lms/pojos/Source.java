package com.lms.pojos;

import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.event.api.JSONAware;
import com.lms.enums.SrcType;
import com.lms.enums.SrcType.LinkType;
import common.utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class Source implements JSONAware {

	@NotNull
	public SrcType type;
	public LinkType linkType;
	public String url;
	// public String thumbnail;
	public String title;
	public String image;
	public String caption;
	public String content;
	// duration will be used only for videos
	public int duration;
	/**
	 * if source is vedantu (document) then save the docId and pageId
	 */
	public String userId;
	public String docId;
	public String pageId;
	public String entityId;
	public LinkInfo linkInfo;

	public Source() {

		this(SrcType.UNKNOWN, "");

	}

	public Source(SrcType type, String thumbnail) {

		this(type, thumbnail, LinkType.UPLOADED);
	}

	public Source(SrcType type, String thumbnail, LinkType linkType) {

		this.type = type;
		// this.thumbnail = thumbnail;
		this.linkType = linkType;
		this.image = "";
		this.url = "";
	}

	@Override
	public JSONObject toJSON() throws JSONException {

		JSONObject json = new JSONObject();

		if (linkInfo == null) {
			putNotNullValue(json, ConstantsGlobal.LINK_INFO, linkInfo.toJSON().toString());
		}

		putNotNullValue(json, ConstantsGlobal.CAPTION, caption);
		putNotNullValue(json, ConstantsGlobal.CONTENT, content);
		putNotNullValue(json, ConstantsGlobal.DOC_ID, docId);
		putNotNullValue(json, ConstantsGlobal.PAGE_ID, pageId);
		putNotNullValue(json, ConstantsGlobal.TITLE, title);
		if (type != null) {
			putNotNullValue(json, ConstantsGlobal.TYPE, type.name());
		}

		return json;
	}

	@Override
	public void fromJSON(JSONObject json) {

		if (json != null) {

			JSONUtils.getJSONAware(linkInfo, json, ConstantsGlobal.LINK_INFO);
			caption = JSONUtils.getString(json, ConstantsGlobal.CAPTION);
			content = JSONUtils.getString(json, ConstantsGlobal.CONTENT);
			docId = JSONUtils.getString(json, ConstantsGlobal.DOC_ID);
			pageId = JSONUtils.getString(json, ConstantsGlobal.PAGE_ID);
			title = JSONUtils.getString(json, ConstantsGlobal.TITLE);
			type = SrcType.valueOfKey(JSONUtils.getString(json, ConstantsGlobal.TYPE));
		}

	}

	private void putNotNullValue(JSONObject json, String key, String value) throws JSONException {

		if (!StringUtils.isEmpty(value)) {
			json.put(key, value);
		}
	}

	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();
		builder.append("Source [type:").append(type).append(", linkType:").append(linkType).append(", url:").append(url)
				// .append(", thumbnail:").append(thumbnail)
				.append(", title:").append(title).append(", image:").append(image).append(", caption:").append(caption)
				.append(", content:").append(content).append(", duration:").append(duration).append(", userId:")
				.append(userId).append(", docId:").append(docId).append(", pageId:").append(pageId)
				.append(", entityId:").append(entityId).append("]");
		return builder.toString();
	}

}
