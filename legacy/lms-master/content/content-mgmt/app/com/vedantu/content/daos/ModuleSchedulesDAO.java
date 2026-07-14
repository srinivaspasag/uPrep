package com.vedantu.content.daos;

import java.util.List;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.vedantu.content.models.ModuleSchedules;
import com.vedantu.content.pojos.requests.ModuleScheduleReq;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;

public class ModuleSchedulesDAO extends VedantuBasicDAO<ModuleSchedules, ObjectId> {
    private static final ALogger           LOGGER   = Logger.of(ModuleSchedulesDAO.class);
    public static final ModuleSchedulesDAO INSTANCE = new ModuleSchedulesDAO(ModuleSchedules.class);

    public ModuleSchedulesDAO(Class<ModuleSchedules> entityClass) {
        super(entityClass);
        // TODO Auto-generated constructor stub
    }

    public ModuleSchedules getSchedule(ModuleScheduleReq req) {
        Query<ModuleSchedules> query = getDS().createQuery(ModuleSchedules.class);
        query.filter("target.type", req.target.type);// SECTION
        query.filter("target.id", req.target.id);
        query.filter("source.type", req.source.type);// CMDSMODULE
        query.filter("source.id", req.source.id);
        query.filter("entity.type", req.entity.type);// CMDSTEST
        query.filter("entity.id", req.entity.id);
        query.filter("recordState", VedantuRecordState.ACTIVE);
        return query.get();
    }

    public List<ModuleSchedules> getSchedule(SrcEntity target, SrcEntity source) {
        Query<ModuleSchedules> query = getDS().createQuery(ModuleSchedules.class);
        query.filter("target.type", target.type);// SECTION
        query.filter("target.id", target.id);
        query.filter("source.type", source.type);// CMDSMODULE
        query.filter("source.id", source.id);
        query.filter("recordState", VedantuRecordState.ACTIVE);
        return query.asList();
    }

    public List<ModuleSchedules> getGlobalSchedule(SrcEntity target, SrcEntity globalSource) {
        Query<ModuleSchedules> query = getDS().createQuery(ModuleSchedules.class);
        query.filter("target.type", target.type);// SECTION
        query.filter("target.id", target.id);
        query.filter("globalSource.type", globalSource.type);// MODULE
        query.filter("globalSource.id", globalSource.id);
        query.filter("recordState", VedantuRecordState.ACTIVE);
        return query.asList();
    }

    public ModuleSchedules getGlobalSchedule(SrcEntity target, SrcEntity globalSource, SrcEntity globalEntity) {
        Query<ModuleSchedules> query = getDS().createQuery(ModuleSchedules.class);
        query.filter("target.type", target.type);// SECTION
        query.filter("target.id", target.id);
        query.filter("globalSource.type", globalSource.type);// MODULE
        query.filter("globalSource.id", globalSource.id);
        query.filter("globalEntity.type", globalEntity.type);// TEST
        query.filter("globalEntity.id", globalEntity.id);
        query.filter("recordState", VedantuRecordState.ACTIVE);
        return query.get();
    }
}
