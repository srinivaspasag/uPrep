package com.lms.pojos;

import com.lms.common.vedantu.commons.pojos.requests.CostRate;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelExtendedInfo;
import com.lms.common.vedantu.enums.AccessScope;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.user.vedantu.user.pojo.UserInfo;

import java.util.List;

public class SDCardGroupInfo extends ModelExtendedInfo {
    public ModelBasicInfo sectionInfo;
    public UserInfo userInfo;
    public UserInfo exportedFor;
    public ModelBasicInfo orgInfo;
    public List<String> cards;
    public long cardSize;
    public AccessScope accessScope;
    public SrcEntity target;
    public long size;
    public CostRate costRate;

    public SDCardGroupInfo(String id, VedantuRecordState recordState, String name,
                           List<String> cards, long cardSize, AccessScope state, long totalSize, long timeCreated,
                           long lastUpdated, CostRate costRate) {

        super(id, recordState, name, timeCreated, lastUpdated);
        this.cards = cards;
        this.cardSize = cardSize;
        this.accessScope = state;
        this.size = totalSize;
        this.costRate = costRate;

    }

    @Override
    public String toString() {

        return "SDCardGroupInfo [sectionInfo=" + sectionInfo + ", userInfo=" + userInfo
                + ", exportedFor=" + exportedFor + ", orgInfo=" + orgInfo + ", cards=" + cards
                + ", cardSize=" + cardSize + "]";
    }

}
