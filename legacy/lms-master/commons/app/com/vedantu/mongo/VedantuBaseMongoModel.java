package com.vedantu.mongo;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.PrePersist;
import com.google.code.morphia.annotations.Transient;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.pojos.responses.ModelExtendedInfo;

public abstract class VedantuBaseMongoModel implements IVedantuModel {

    @Id
    public ObjectId           id;

    @Transient
    public static String      TIME_CREATED = "timeCreated";
    public static String      LAST_UPDATED = "lastUpdated";
    public static String      RECORD_STATE = "recordState";

    public long               timeCreated  = 0L;

    public long               lastUpdated  = 0L;

    public VedantuRecordState recordState  = VedantuRecordState.ACTIVE;

    public String _getStringId() {

        return null != id ? id.toString() : StringUtils.EMPTY;
    }

    @PrePersist
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
        return null != v && StringUtils.equals(_getStringId(), v._getStringId());
    }

    @Override
    public int hashCode() {

        return _getStringId().hashCode();
    }
}
