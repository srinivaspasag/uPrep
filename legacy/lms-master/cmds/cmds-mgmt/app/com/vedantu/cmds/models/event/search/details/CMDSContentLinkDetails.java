package com.vedantu.cmds.models.event.search.details;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.ScheduleInfo;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.relationships.IRelationshipSearchDetails;

public class CMDSContentLinkDetails implements IRelationshipSearchDetails {

    public String       id;
    public String       userId;
    public SrcEntity    source;
    public SrcEntity    target;
    public Scope        scope;
    public boolean      downloadble;
    public ScheduleInfo schedule;
    public long         timeCreated;
    public String       globalLinkId;
    public long         position;

    public CMDSContentLinkDetails() {

    }

    public CMDSContentLinkDetails(String linkId, String userId, SrcEntity source, SrcEntity target,
            Scope scope, long timeCreated,long position) {

        this.userId = userId;
        this.source = source;
        this.target = target;
        this.scope = scope;
        this.timeCreated = timeCreated;
        this.id = linkId;
        this.position= position;
    }

    @Override
    public QueryBuilder _getEsQuery() {

        QueryBuilder query = null;
        if (id == null ||  id.isEmpty()) {
            query = QueryBuilders
                    .boolQuery()
                    .must(QueryBuilders.termQuery(
                            ConstantsGlobal.SOURCE + "." + ConstantsGlobal.ID, source.id))
                    .must(QueryBuilders.termQuery("target.id", target.id));
        } else {
            query = QueryBuilders
                    .boolQuery()
                    .must(QueryBuilders.termQuery(ConstantsGlobal.ID, id))
                    .must(QueryBuilders.termQuery(
                            ConstantsGlobal.SOURCE + "." + ConstantsGlobal.ID, source.id));
        }

        return query;
    }

}
