package com.vedantu.content.daos.challenges;

import org.apache.commons.lang.time.DateUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.enums.MultiplierPowerRule;
import com.vedantu.content.enums.challenges.MultiplierPowerType;
import com.vedantu.content.enums.challenges.MultiplierPowerValidityType;
import com.vedantu.content.models.challenges.MultiplierPower;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.MongoManager.SortOrder;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuDBResult;

public class MultiplierPowerDAO extends VedantuBasicDAO<MultiplierPower, ObjectId> {

    private static final ALogger           LOGGER   = Logger.of(MultiplierPowerDAO.class);
    public final static MultiplierPowerDAO INSTANCE = new MultiplierPowerDAO();

    private MultiplierPowerDAO() {

        super(MultiplierPower.class);
    }

    public MultiplierPower getActiveMultiplierPower(String userId, SrcEntity parent) {

        MultiplierPower activeMultiplierPower = null;
        DBObject query = new BasicDBObject(ConstantsGlobal.USER_ID, userId);
        query.put(ConstantsGlobal.PARENT_DOT_ID, parent.id);
        query.put(ConstantsGlobal.PARENT_DOT_TYPE, parent.type.name());
        for (MultiplierPowerType powerType : MultiplierPowerType.values()) {
            query.put(ConstantsGlobal.TYPE, powerType.name());
            VedantuDBResult<MultiplierPower> results = getInfos(query, null, MongoManager.NO_START,
                    MongoManager.NO_LIMIT, new BasicDBObject(ConstantsGlobal.TIME_CREATED,
                            SortOrder.DESC.getValue()));
            for (MultiplierPower multiplierPower : results.results) {
                activeMultiplierPower = getSuperMultiplierPower(activeMultiplierPower,
                        multiplierPower);
            }
        }
        return activeMultiplierPower;
    }

    // this method should be only called after the rank of a challenge is
    // calculated
    public void allowtMultiplierPower(String userId, String challengeId, SrcEntity parent) {

        LOGGER.info("allowting multiplier power to user[" + userId + "]");
        for (MultiplierPowerRule powerRule : MultiplierPowerRule.values()) {
            LOGGER.info("trying to allowat [" + powerRule + "] power to user[" + userId
                    + "] for challenge[" + challengeId + "]");
            MultiplierPower power = powerRule.allowatMulitplierPower(userId, challengeId, parent);
            if (power != null) {
                LOGGER.info("successfully allowated [" + powerRule + "] power to user[" + userId
                        + "] for challenge[" + challengeId + "]");
                save(power);
            } else {
                LOGGER.info("user[" + userId + "] does not qualify for [" + powerRule
                        + "] multiplierPower for challenge[" + challengeId + "]");
            }
        }
    }

    private MultiplierPower getSuperMultiplierPower(MultiplierPower activePower,
            MultiplierPower comparablePower) {

        // comparablePower can not be null
        if (activePower == null) {
            return isActivePower(comparablePower) ? comparablePower : null;
        }
        return isActivePower(comparablePower)
                && comparablePower.type.getMultiplier() > activePower.type.getMultiplier() ? comparablePower
                : activePower;
    }

    private boolean isActivePower(MultiplierPower multiplierPower) {

        boolean active = false;
        if (multiplierPower.validityType == MultiplierPowerValidityType.CHALLENGE) {
            active = multiplierPower.useCount < multiplierPower.validFor;
        } else if (multiplierPower.validityType == MultiplierPowerValidityType.DAYS) {
            long activeTime = multiplierPower.validFor * DateUtils.MILLIS_PER_DAY;
            active = System.currentTimeMillis() < (multiplierPower.timeCreated + activeTime);
        }
        return active;
    }
}
