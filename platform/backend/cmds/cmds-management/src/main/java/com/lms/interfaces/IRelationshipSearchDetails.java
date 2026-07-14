package com.lms.interfaces;

import org.springframework.data.mongodb.core.query.Query;

public interface IRelationshipSearchDetails {


    Query _getEsQuery();
}
