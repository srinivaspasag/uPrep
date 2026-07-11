package com.vedantu.commons.relationships;

import org.elasticsearch.index.query.QueryBuilder;

public interface IRelationshipSearchDetails {

    public abstract QueryBuilder _getEsQuery();
}
