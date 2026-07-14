package com.vedantu.ext.cmds.managers;

import java.util.Arrays;
import java.util.Map;

import javax.servlet.ServletException;

import com.vedantu.ext.cmds.db.datamanagers.FlashRecordDataManager;
import com.vedantu.ext.cmds.db.datamanagers.SDCardDataManger;
import com.vedantu.ext.cmds.db.datamanagers.SDCardGroupDataManager;
import com.vedantu.ext.cmds.db.datamanagers.SyncInfoDataManager;
import com.vedantu.ext.cmds.db.models.FlashRecordInfo;
import com.vedantu.ext.cmds.db.models.Organization;
import com.vedantu.ext.cmds.db.models.SDCard;
import com.vedantu.ext.cmds.db.models.SDCardGroup;
import com.vedantu.ext.cmds.db.models.SyncInfo;
import com.vedantu.ext.cmds.pojo.responses.GetSDCardGroupInfoRes;
import com.vedantu.ext.cmds.pojo.responses.GetSDCardGroupRes;
import com.vedantu.ext.cmds.pojo.responses.GetSDCardGroupsRes;
import com.vedantu.ext.cmds.pojo.responses.GetSDCardInfoRes;
import com.vedantu.ext.cmds.utils.commons.StringUtils;
import com.vedantu.ext.cmds.utils.web.WebCommunicator;
import com.vedantu.ext.cmds.web.ReqAction;
import com.vedantu.ext.cmds.web.VedantuHttpResponse;

public class SDCardsManager extends AbstractManager {

    public static final SDCardsManager INSTANCE = new SDCardsManager();

    private SDCardsManager() {

        super();
    }

    public GetSDCardGroupInfoRes getSDCardGroupInfo(final Organization org,
            final Map<String, Object> httpParams) throws ServletException {

        VedantuHttpResponse webRes = WebCommunicator.getResult(ReqAction.GET_SD_CARD_GROUP_INFO,
                httpParams);
        checkForErrorResponse(webRes);
        LOGGER.debug("SD Card Response " + webRes.getResult().toString());
        GetSDCardGroupInfoRes res = new GetSDCardGroupInfoRes();
        webRes.populateResult(res);

        for (GetSDCardInfoRes cardInfo : res.getCards()) {
            LOGGER.debug("SD Card Response count " + cardInfo.getCount());
            SDCard card = new SDCard(org._id, cardInfo.getName(), cardInfo.getId(),
                    cardInfo.getGroupId(), cardInfo.getSize(), cardInfo.getContentSize(),
                    cardInfo.getTimeCreated(), cardInfo.getCount());
            SDCardGroup group = SDCardGroupDataManager.INSTANCE.getSDCardGroup(cardInfo
                    .getGroupId());
            try {
                card = SDCardDataManger.INSTANCE.upsert(card);
                FlashRecordInfo flashInfo = new FlashRecordInfo(card.orgKeyId, group.targetId,
                        card.id, card.groupId);
                FlashRecordDataManager.INSTANCE.upsert(flashInfo);

            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return res;
    }

    // public void loadSDCardGroupsRes(final Organization org, final Map<String, Object> httpParams,
    // final String targetId, final String targetType) {
    //
    // ExecutorUtils.executeTask(new Runnable() {
    //
    // @Override
    // public void run() {
    //
    // try {
    // getSDCardGroupsRes(org, httpParams, targetId, targetType);
    // } catch (ServletException e) {
    // LOGGER.error(e.getMessage(), e);
    // }
    // }
    // });
    // }

    public GetSDCardGroupsRes getSDCardGroupsRes(Organization org, Map<String, Object> httpParams,
            String targetId, String targetType) throws ServletException {

        if (StringUtils.isEmpty(targetId)) {
            throw new ServletException("missing targetId");
        }
        SyncInfo syncInfo = SyncInfoDataManager.INSTANCE.getSyncInfo(getSyncKey("sdcardGroup",
                Arrays.asList(targetId, targetType)));
        if (syncInfo != null) {
            httpParams.put(FIELD_ADDED_AFTER, syncInfo.syncTime);
        }

        VedantuHttpResponse webRes = WebCommunicator.getResult(ReqAction.GET_SD_CARD_GROUPS, httpParams);
        checkForErrorResponse(webRes);
        GetSDCardGroupsRes res = new GetSDCardGroupsRes();
        webRes.populateResult(res);
        int sdcardgroupIndex = 0;
        for (GetSDCardGroupRes cRes : res.list) {
            LOGGER.debug("SD Card group index " + sdcardgroupIndex++);
            SDCardGroup sdCardGroup = new SDCardGroup(org._id, cRes.name, cRes.id, cRes.target.id,
                    cRes.target.type == null ? StringUtils.EMPTY : cRes.target.type.name(),
                    cRes.size, cRes.cardSize, cRes.noOfCards, cRes.cardIds, cRes.timeCreated);
            try {
                sdCardGroup = SDCardGroupDataManager.INSTANCE.upsert(sdCardGroup);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        if (syncInfo == null) {
            syncInfo = new SyncInfo(org._id, getSyncKey("sdcardGroup",
                    Arrays.asList(targetId, targetType)), System.currentTimeMillis());
            try {
                syncInfo = SyncInfoDataManager.INSTANCE.upsert(syncInfo);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return res;
    }
}
