package com.vedantu.cmds.daos;

import java.util.List;

import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.vedantu.cmds.models.SDCardGroup;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.enums.AccessScope;

public class SDCardGroupDAO extends VedantuBasicDAO<SDCardGroup, ObjectId> {

    private static final ALogger       LOGGER   = Logger.of(SDCardGroupDAO.class);

    public static final SDCardGroupDAO INSTANCE = new SDCardGroupDAO();

    private SDCardGroupDAO() {

        super(SDCardGroup.class);
    }

    public List<SDCardGroup> getGroups(String sectionId, AccessScope state, int start,
            int size, long addedAfter, MutableLong totalExportRecords) {

        Query<SDCardGroup> groupQuery = getQuery();

        groupQuery = groupQuery.field("target.type").equal(EntityType.SECTION);

        groupQuery = groupQuery.field("target.id").equal(sectionId);

        groupQuery = groupQuery.field(RECORD_STATE).equal(VedantuRecordState.ACTIVE);

        if (state != null) {
            groupQuery = groupQuery.field("state").equal(state.name());
        }
        if (addedAfter > 0) {
            groupQuery.field(ConstantsGlobal.TIME_CREATED).greaterThanOrEq(addedAfter);
        }
        totalExportRecords.setValue(groupQuery.countAll());
        LOGGER.debug("Query" + groupQuery.toString());
        return groupQuery.offset(start).limit(size).asList();
    }

}
