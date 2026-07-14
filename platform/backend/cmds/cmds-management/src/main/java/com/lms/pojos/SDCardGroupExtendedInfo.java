package com.lms.pojos;

import com.lms.common.vedantu.commons.pojos.requests.CostRate;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.enums.AccessScope;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.user.vedantu.user.pojo.UserInfo;

import java.util.List;

public class SDCardGroupExtendedInfo extends SDCardGroupInfo {

    public ModelBasicInfo sectionInfo;
    public UserInfo userInfo;
    public UserInfo exportedFor;

    public List<SDCardInfo> cardInfos;
    public long cardSize;
    public AccessScope state;
    public long size;


    public SDCardGroupExtendedInfo(String id, VedantuRecordState recordState, String name,
                                   long cardSize, List<String> cards, AccessScope state, long totalSize, long timeCreated,
                                   long lastUpdated, CostRate costRate) {

        // super(id, recordState, name, timeCreated, lastUpdated);
        super(id, recordState, name, cards, cardSize, state, totalSize, timeCreated, lastUpdated, costRate);
        this.cardSize = cardSize;
        this.state = state;
        this.size = totalSize;

    }

    @Override
    public String toString() {

        return "SDCardGroupInfo [sectionInfo=" + sectionInfo + ", userInfo=" + userInfo
                + ", exportedFor=" + exportedFor + ", cardInfos=" + cardInfos
                + ", cardSize=" + cardSize + "]";
    }

}
