package com.vedantu.content.pojos.tests;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.boards.BoardType;
import com.vedantu.commons.events.apis.JSONAware;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.content.apis.IAnalyticsBoardMember;
import com.vedantu.content.pojos.BoardAnalyticsInfo;

public class BoardQus implements JSONAware, Serializable, IAnalyticsBoardMember {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	@Required
	public String				id;
	@Required
	public String				name;
	public int					qusCount;
	public int					totalMarks;
	public List<String>			qIds;

	public BoardQus() {
		this(StringUtils.EMPTY, StringUtils.EMPTY, 0);
	}

	public BoardQus(String id, String name, int qusCount) {
		this.id = id;
		this.name = name;
		this.qusCount = qusCount;
		this.qIds = new ArrayList<String>();
	}

	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put(ConstantsGlobal.ID, id);
		json.put(ConstantsGlobal.NAME, name);
		json.put(ConstantsGlobal.QUS_COUNT, qusCount);
		json.put(ConstantsGlobal.QIDS, qIds);
		return json;
	}

	@Override
	public void fromJSON(JSONObject json) {
		id = JSONUtils.getString(json, ConstantsGlobal.ID);
		name = JSONUtils.getString(json, ConstantsGlobal.NAME);
		qusCount = JSONUtils.getInt(json, ConstantsGlobal.QUS_COUNT);
		qIds = JSONUtils.getList(json, ConstantsGlobal.QIDS);
	}

	@Override
	public int hashCode() {
		return ((id == null) ? 0 : id.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass())
			return false;
		BoardQus other = (BoardQus) obj;
		return StringUtils.equals(id, other.id);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BoardQus [id:").append(id).append(", name:")
				.append(name).append(", qusCount:").append(qusCount)
				.append(", qIds:").append(qIds).append("]");
		return builder.toString();
	}

	@Override
	public List<? extends IAnalyticsBoardMember> _getChildrenBoards() {
		return null;
	}

	@Override
	public BoardAnalyticsInfo _getEntity() {
		final BoardAnalyticsInfo entity = new BoardAnalyticsInfo(name, id,
				BoardType.TOPIC);
		return entity;
	}

	@Override
	public int _getTotalMarks() {
		return totalMarks;
	}

	@Override
	public int _getQusCount() {
		return qusCount;
	}

}
