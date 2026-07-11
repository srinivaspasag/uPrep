package com.vedantu.content.pojos.requests;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.requests.AbstractListReq;
import com.vedantu.content.enums.search.SearchResultType;

public abstract class AbstractContentSearchReq extends AbstractListReq {

    public String           targetUserId;

    public SrcEntity        contentSrc;
    public String           query;
    public List<String>     brdIds;
    public List<String>     tags;
    public String           brdType;     // TODO: check where can this param be used..

    public List<String>     includeTypes; // This is only for field "type"
    public List<String>     includeModes;
    public List<String>     includeDifficulty;

    public List<String>     excludeTypes;

    public List<String>     excludeIds;

    // org related request params
    public String           orgId;
    public String           programId;
    public String           centerId;
    public String           sectionId;

    // This field is used to show learnpedia questions or not for other organisations
    public List<String>     scope;

    @Required
    public SearchResultType resultType;

    public boolean          allBrds;
    public boolean          facet;

    public String _getResultForUserId() {

        return StringUtils.isEmpty(targetUserId) ? userId : targetUserId;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{targetUserId:").append(targetUserId).append(", contentSrc:")
                .append(contentSrc).append(", query:").append(query).append(", brdIds:")
                .append(brdIds).append(", tags:").append(tags).append(", brdType:").append(brdType)
                .append(", includeTypes:").append(includeTypes).append(", excludeTypes:")
                .append(excludeTypes).append(", excludeIds:").append(excludeIds)
                .append(", sortOrder:").append(sortOrder).append(", orderBy:").append(orderBy)
                .append(", orgId:").append(orgId).append(", programId:").append(programId)
                .append(", centerId:").append(centerId).append(", sectionId:").append(sectionId)
                .append(", resultType:").append(resultType).append(", allBrds:").append(allBrds)
                .append(", facet:").append(facet).append("}");
        return builder.toString();
    }

}
