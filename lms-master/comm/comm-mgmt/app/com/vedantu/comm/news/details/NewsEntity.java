package com.vedantu.comm.news.details;

import org.codehaus.jackson.annotate.JsonManagedReference;
import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;

public class NewsEntity extends SrcEntity {
	@JsonManagedReference
	public SrcEntity	contentSrc	= null;

	public NewsEntity() {
	}

	public NewsEntity(NewsEntity toBeCopied) {
		this(toBeCopied.id, toBeCopied.type);
		if (toBeCopied.contentSrc != null) {
			this.contentSrc = new SrcEntity();
			this.contentSrc.id = toBeCopied.contentSrc.id;
			this.contentSrc.type = toBeCopied.contentSrc.type;

		}
	}

	public NewsEntity(String id, EntityType type) {
		super(type, id);
	}

	/**
	 * Level 0 parent
	 * 
	 * @return
	 */
	public NewsEntity getBaseParent() {
		return this;
	}

	@Override
	public String toString() {
		return "NewsEntity [id=" + id + ", type=" + type + "]";
	}

	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		return json;
	}

	@Override
	public void fromJSON(JSONObject json) {
		super.fromJSON(json);

	}
}
