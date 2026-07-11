package com.vedantu.cmds.managers;

import com.vedantu.cmds.models.CMDSContentLink;
import com.vedantu.cmds.models.event.search.details.CMDSContentLinkDetails;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuRecordState;

public class CMDSContentLinkManager extends AbstractCMDSContentManager {

    public static CMDSContentLinkManager INSTANCE = new CMDSContentLinkManager();

    private CMDSContentLinkManager() {

    }

   
    public void reIndex(CMDSContentLink link) {

        CMDSContentLinkDetails libraryContentLinkDetails = new CMDSContentLinkDetails(
                link._getStringId(), link.userId, link.source, link.target, link.getScope(),
                link.timeCreated,link.position);
        SrcEntity resource = new SrcEntity(EntityType.CMDSRESOURCE, getResourceId(link.source));
        if (link.recordState == VedantuRecordState.ACTIVE) {

            updateUserActionMappintToEs(libraryContentLinkDetails, resource, link.linkType.name()
                    .toLowerCase(), EventActionType.UPDATE, null);
        } else {
            updateUserActionMappintToEs(libraryContentLinkDetails, resource, link.linkType.name()
                    .toLowerCase(), EventActionType.REMOVE, null);
        }
    }

    @Override
    public void prePublish(SrcEntity content) {

    }

    @Override
    public void postPublish(VedantuBaseMongoModel model) {

    }

    @Override
    protected VedantuBaseMongoModel publish(String userId, String orgId, SrcEntity content)
            throws VedantuException {

        return null;
    }
}
