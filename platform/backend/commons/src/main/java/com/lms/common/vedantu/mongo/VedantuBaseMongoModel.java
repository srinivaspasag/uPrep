package com.lms.common.vedantu.mongo;

import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelExtendedInfo;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;

@Setter
@Getter
public class VedantuBaseMongoModel {
    @Id
    public ObjectId id;

    @Transient
    public static String      TIME_CREATED = "timeCreated";
    public static String      LAST_UPDATED = "lastUpdated";
    public static String      RECORD_STATE = "recordState";

    public long               timeCreated  = 0L;

    public long               lastUpdated  = 0L;

    public VedantuRecordState recordState  = VedantuRecordState.ACTIVE;

    public String _getStringId() {

        return null != id ? id.toString() : HardCodedConstants.emptyString;
    }


    protected void prePersist() {

        long now = System.currentTimeMillis();
        timeCreated = (0L == timeCreated) ? now : timeCreated;
        lastUpdated = (0L == lastUpdated) ? timeCreated : now;
    }

    public ModelBasicInfo toBasicInfo() {

        return null;
    }

    public ModelExtendedInfo toExtendedInfo() {

        return null;
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof VedantuBaseMongoModel)) {
            return false;
        }
        VedantuBaseMongoModel v = (VedantuBaseMongoModel) o;
        return null != v && _getStringId().equals(v._getStringId());
    }

    @Override
    public int hashCode() {

        return _getStringId().hashCode();
    }
}
