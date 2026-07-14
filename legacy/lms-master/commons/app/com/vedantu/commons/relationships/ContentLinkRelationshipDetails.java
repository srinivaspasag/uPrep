package com.vedantu.commons.relationships;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EncryptionLevel;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.ScheduleInfo;
import com.vedantu.commons.pojos.SrcEntity;

public class ContentLinkRelationshipDetails implements IRelationshipSearchDetails {

    public SrcEntity       entity;
    public String          userId;
    public SrcEntity       dst;
    public Scope           scope;
    public ScheduleInfo    schedule;
    public long            timeCreated;
    public long            lastUpdated;
    public boolean         downloadble;
    public EncryptionLevel encLevel;
    public long            position;

    public ContentLinkRelationshipDetails(String userId, SrcEntity entity, SrcEntity dst,
            Scope scope) {

        this.userId = userId;
        this.dst = dst;
        this.entity = entity;
        this.scope = scope;
        this.timeCreated = System.currentTimeMillis();
        this.lastUpdated = System.currentTimeMillis();
    }

    @Override
    public QueryBuilder _getEsQuery() {

        QueryBuilder query = QueryBuilders
                .boolQuery()
                .must(QueryBuilders.termQuery(ConstantsGlobal.ENTITY + "." + ConstantsGlobal.ID,
                        entity.id)).must(QueryBuilders.termQuery("dst.id", dst.id));
        return query;
    }

}
