package com.vedantu.commons.relationships;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.vedantu.commons.constants.ConstantsGlobal;

public class EntityUserActionRelationshipSearchDetails implements
		IRelationshipSearchDetails {

	public String userId;
	public String dstId;
	public long timeCreated;

	public EntityUserActionRelationshipSearchDetails(String userId, String dstId) {
		this.userId = userId;
		this.dstId = dstId;
		this.timeCreated = System.currentTimeMillis();
	}

	@Override
	public QueryBuilder _getEsQuery() {
		QueryBuilder query = QueryBuilders.boolQuery()
				.must(QueryBuilders.termQuery(ConstantsGlobal.USER_ID, userId))
				.must(QueryBuilders.termQuery(ConstantsGlobal.DST_ID, dstId));
		return query;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("userId:").append(userId).append(", dstId:")
				.append(dstId).append(",timeCreated:" + timeCreated);
		return builder.toString();
	}
}
