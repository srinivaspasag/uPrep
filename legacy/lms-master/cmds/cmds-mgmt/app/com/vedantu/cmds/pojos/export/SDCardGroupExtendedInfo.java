package com.vedantu.cmds.pojos.export;

import java.util.List;

import com.vedantu.commons.pojos.CostRate;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.enums.AccessScope;
import com.vedantu.user.pojos.UserInfo;

public class SDCardGroupExtendedInfo extends SDCardGroupInfo {

    public ModelBasicInfo     sectionInfo;
    public UserInfo           userInfo;
    public UserInfo           exportedFor;

    public List<SDCardInfo>   cardInfos;
    public long               cardSize;
    public AccessScope state;
    public long               size;
    

    public SDCardGroupExtendedInfo(String id, VedantuRecordState recordState, String name,
            long cardSize,List<String> cards, AccessScope state, long totalSize, long timeCreated,
            long lastUpdated,CostRate costRate) {

       // super(id, recordState, name, timeCreated, lastUpdated);
        super(id,recordState,name,cards,cardSize,state,totalSize,timeCreated,lastUpdated,costRate);
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
