package com.vedantu.cmds.managers;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.daos.CmdsContentLinkDAO;
import com.vedantu.cmds.daos.SDCardDAO;
import com.vedantu.cmds.daos.SDCardGroupDAO;
import com.vedantu.cmds.enums.CmdsContentLinkType;
import com.vedantu.cmds.models.SDCard;
import com.vedantu.cmds.models.SDCardGroup;
import com.vedantu.cmds.models.event.details.SDCardDetails;
import com.vedantu.cmds.pojos.requests.DeleteSdCardGroupReq;
import com.vedantu.cmds.pojos.requests.exports.GetSDCardGroupReq;
import com.vedantu.cmds.pojos.requests.exports.GetSDCardGroupsReq;
import com.vedantu.cmds.pojos.requests.exports.MarkSDGroupReq;
import com.vedantu.cmds.pojos.requests.exports.ScheduleSDGroupCreateReq;
import com.vedantu.cmds.pojos.responses.DeleteSdCardGroupRes;
import com.vedantu.cmds.pojos.responses.GetExportRecordRes;
import com.vedantu.cmds.pojos.responses.GetExportRecordsRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.daos.EntityOperationStatusDAO;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.OperationType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.models.mongo.EntityOperationStatus;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ActionTakenRes;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.enums.AccessScope;
import com.vedantu.user.managers.AbstractVedantuEventManager;

public class SDCardGroupManager extends AbstractVedantuEventManager {

    private final static ALogger LOGGER = Logger.of(SDCardGroupManager.class);

    public GetExportRecordRes create(ScheduleSDGroupCreateReq request) throws VedantuException {

        MutableLong orgScopedTotalContents = new MutableLong();

        CmdsContentLinkDAO.INSTANCE.getCmdsContentLinks(null, request.target,
                CmdsContentLinkType.ADDED, StringUtils.EMPTY, Scope.ORG, MongoManager.NO_START,
                MongoManager.NO_LIMIT, VedantuRecordState.ACTIVE, orgScopedTotalContents);

        MutableLong totalActiveContent = new MutableLong();
        CmdsContentLinkDAO.INSTANCE.getCmdsContentLinks(null, request.target,
                CmdsContentLinkType.ADDED, StringUtils.EMPTY, Scope.UNKNOWN, MongoManager.NO_START,
                MongoManager.NO_LIMIT, VedantuRecordState.ACTIVE, totalActiveContent);
        LOGGER.debug(" Total Active content" + totalActiveContent.longValue() + " "
                + orgScopedTotalContents.longValue());
        if (orgScopedTotalContents.longValue() < totalActiveContent.longValue()) {
            throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED, "All content not published");
        }

        SDCardGroup sdCardGroup = new SDCardGroup(request.name);

        sdCardGroup.recordState = VedantuRecordState.ACTIVE;
        sdCardGroup.completed = false;
        sdCardGroup.accessScope = AccessScope.CLOSED;
        sdCardGroup.scope = Scope.ORG;
        sdCardGroup.target = request.target;
        sdCardGroup.cardSize = request.maxCardSize;
        sdCardGroup.userId = request.userId;
        sdCardGroup.contentSrc = new SrcEntity(EntityType.ORGANIZATION, request.orgId);

        EntityOperationStatus status = new EntityOperationStatus();
        status.numOfSteps = totalActiveContent.intValue();
        status.oType = OperationType.SD_CARD_CREATION;
        status.type = EntityType.SDCARD;
        EntityOperationStatusDAO.INSTANCE.save(status);
        sdCardGroup.jobId = status._getStringId();
        SDCardGroupDAO.INSTANCE.save(sdCardGroup);

        SDCardDetails details = new SDCardDetails();
        details.groupId = sdCardGroup._getStringId();

        generateEventAysc(request.userId, details, EventType.SD_CARD_SPLIT);

        GetExportRecordRes response = new GetExportRecordRes();
        response.recordInfo = sdCardGroup.toExtendedInfo();
        response.jobId = sdCardGroup.jobId;
        return response;
    }

    public GetExportRecordsRes getSDCardGroups(GetSDCardGroupsReq request) throws VedantuException {

        GetExportRecordsRes response = new GetExportRecordsRes();

        MutableLong totalExportRecords = new MutableLong();
        List<SDCardGroup> records = SDCardGroupDAO.INSTANCE.getGroups(request.sectionId,
                request.state, request.start, request.size, request.addedAfter, totalExportRecords);

        response.totalHits = totalExportRecords.longValue();
        GetExportRecordRes sdCardGroupResponse = null;
        for (SDCardGroup record : records) {

            sdCardGroupResponse = new GetExportRecordRes();
            sdCardGroupResponse.recordInfo = record.toExtendedInfo();
            response.list.add(sdCardGroupResponse);

        }
        response.totalHits = totalExportRecords.longValue();
        return response;
    }

    public GetExportRecordRes getSDCardGroup(GetSDCardGroupReq request) throws VedantuException {

        GetExportRecordRes response = new GetExportRecordRes();
        SDCardGroup record = SDCardGroupDAO.INSTANCE.getById(request.groupId);
        if (record != null) {
            response.recordInfo = record.toExtendedInfo();
        }
        else{
            throw new VedantuException(VedantuErrorCode.NO_CONTENT_FOUND,"no group found for groupId");
        }
        return response;
    }

    public ActionTakenRes mark(MarkSDGroupReq request) throws VedantuException {

        ActionTakenRes response = new ActionTakenRes();

        SDCardGroup group = SDCardGroupDAO.INSTANCE.getById(request.groupId);
        if (group == null) {
            throw new VedantuException(VedantuErrorCode.NO_CONTENT_FOUND, "SD group not found");
        }
        group.accessScope = request.state;
        if (request.costRate != null) {
            group.costRate = request.costRate;
        }
        SDCardGroupDAO.INSTANCE.updateModel(group,
                Arrays.asList(SDCardGroup.ACCESS_SCOPE, SDCardGroup.COST_RATE));
        response.done = true;
        return response;
    }

    public static DeleteSdCardGroupRes deleteSdCardGroup(DeleteSdCardGroupReq request) {
        DeleteSdCardGroupRes response = new DeleteSdCardGroupRes();
        SDCardGroup sdCardGroup = SDCardGroupDAO.INSTANCE.getById(request.sdCardGroupId);
        if (sdCardGroup == null) {
            LOGGER.error("Sd Card group not found for SD card group id : " + request.sdCardGroupId);
            response.deleted = false;
            return response;
        }
        sdCardGroup.recordState = VedantuRecordState.DELETED;
        sdCardGroup.name = sdCardGroup.name + "_" + sdCardGroup._getStringId();
        SDCardGroupDAO.INSTANCE.save(sdCardGroup);

        for (String sdCardId : sdCardGroup.cards) {
            SDCard sdCard = SDCardDAO.INSTANCE.getById(sdCardId);
            if (sdCard == null) {
                LOGGER.error("Sd Card not found for SD card id : " + sdCardId);
                continue;
            }
            sdCard.recordState = VedantuRecordState.DELETED;
            SDCardDAO.INSTANCE.save(sdCard);
        }
        response.deleted = true;
        return response;
    }
}
