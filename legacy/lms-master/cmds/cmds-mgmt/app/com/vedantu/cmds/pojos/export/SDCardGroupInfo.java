package com.vedantu.cmds.pojos.export;

import java.util.List;

import com.vedantu.commons.pojos.CostRate;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.pojos.responses.ModelExtendedInfo;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.enums.AccessScope;
import com.vedantu.user.pojos.UserInfo;

public class SDCardGroupInfo extends ModelExtendedInfo {

    public ModelBasicInfo sectionInfo;
    public UserInfo       userInfo;
    public UserInfo       exportedFor;
    public ModelBasicInfo orgInfo;
    public List<String>   cards;
    public long           cardSize;
    public AccessScope    accessScope;
    public SrcEntity      target;
    public long           size;
    public CostRate       costRate;

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
