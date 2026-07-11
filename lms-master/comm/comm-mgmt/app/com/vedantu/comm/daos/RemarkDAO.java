package com.vedantu.comm.daos;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.vedantu.comm.models.mongo.Remark;
import com.vedantu.comm.pojos.RemarkInfo;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.mongo.VedantuBasicDAO;

public class RemarkDAO extends VedantuBasicDAO<Remark, ObjectId> {

    private static final ALogger  LOGGER      = Logger.of(RemarkDAO.class);

    public static final RemarkDAO INSTANCE    = new RemarkDAO();

    public static final String    UNKNOWN_DOB = "1970-01-01";

    private RemarkDAO() {

        super(Remark.class);
    }

    public List<Remark> getRemarksFor(String provideeId, String providerId, int start, int size,
            boolean ascending, MutableLong totalHits) {

        LOGGER.debug(" Querying in db now ");

        Query<Remark> remarkQuery = getDS().createQuery(Remark.class);
        if (StringUtils.isNotEmpty(provideeId)) {
            remarkQuery = remarkQuery.filter("provideeId", provideeId);
        }
        if (StringUtils.isNotEmpty(providerId)) {
            remarkQuery = remarkQuery.filter("providerId", providerId);
        }

        remarkQuery = remarkQuery.offset(start).limit(size);
        if (ascending) {
            remarkQuery.order("timeCreated");
        } else {
            remarkQuery.order("-timeCreated");
        }
        LOGGER.debug(remarkQuery.toString());
        totalHits.setValue(remarkQuery.countAll());
        return remarkQuery.asList();
    }

    public Remark addRemark(String provideeId, String providerId, String content, String orgId) {

        Remark remark = new Remark(providerId, provideeId, content, orgId);
        save(remark);
        return remark;
    }

    @Override
    public ModelBasicInfo getBasicInfo(String id) {

        Remark remark = getById(id);
        if (remark == null) {
            return null;
        }
        return getBasicInfo(remark);
    }

    public ModelBasicInfo getBasicInfo(Remark info) {

        RemarkInfo remark = new RemarkInfo(info._getStringId(), null, EntityType.REMARK,
                info.timeCreated, info.lastUpdated, info.providerId, 0, info.recordState);
        remark.content = info.content;

        return remark;
    }

}
