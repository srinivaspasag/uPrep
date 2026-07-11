package com.lms.models.events.searchdetails;

import com.lms.common.pojos.ScheduleInfo;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.Scope;
import com.lms.interfaces.IRelationshipSearchDetails;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.query.Query;

@Setter
@Getter
public class CMDSContentLinkDetails implements IRelationshipSearchDetails {

    public String id;
    public String userId;
    public SrcEntity source;
    public SrcEntity target;
    public Scope scope;
    public boolean downloadble;
    public ScheduleInfo schedule;
    public long timeCreated;
    public String globalLinkId;
    public long position;

    public CMDSContentLinkDetails() {

    }

    public CMDSContentLinkDetails(String linkId, String userId, SrcEntity source, SrcEntity target,
                                  Scope scope, long timeCreated, long position) {

        this.userId = userId;
        this.source = source;
        this.target = target;
        this.scope = scope;
        this.timeCreated = timeCreated;
        this.id = linkId;
        this.position = position;
    }


    @Override
    public Query _getEsQuery() {
        return null;
    }
}
