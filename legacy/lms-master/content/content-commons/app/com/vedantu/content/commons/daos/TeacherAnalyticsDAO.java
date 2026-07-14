package com.vedantu.content.commons.daos;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.vedantu.content.models.TeacherAnalytics;
import com.vedantu.mongo.VedantuBasicDAO;

public class TeacherAnalyticsDAO extends VedantuBasicDAO<TeacherAnalytics, ObjectId> {

    private static final ALogger            LOGGER   = Logger.of(TeacherAnalyticsDAO.class);

    public static final TeacherAnalyticsDAO INSTANCE = new TeacherAnalyticsDAO();

    private TeacherAnalyticsDAO() {
        super(TeacherAnalytics.class);
    }

    public Queue<TeacherAnalytics> getTeacherAnalyticsQueue(String subjectId) {

        Query<TeacherAnalytics> query = getQuery();
        query.field("boardId").equal(subjectId);
        query.order("lastAssaignedTime");
        List<TeacherAnalytics> teachersList = query.asList();
        Queue<TeacherAnalytics> queue = new LinkedList<TeacherAnalytics>(teachersList);
        return queue;
    }

    public TeacherAnalytics getByTeacherId(String teacherId) {
        TeacherAnalytics teacher = getQuery().filter("teacherOrgMemberId", teacherId).get();
        if (null == teacher) {
            LOGGER.error("cannot find teacher for teacherId: " + teacherId);
        }
        return teacher;
    }

    public TeacherAnalytics cleanCurrentAssignedDoubt(String teacherId){
        TeacherAnalytics teacher = getQuery().filter("teacherOrgMemberId", teacherId).get();
        if (null == teacher) {
            LOGGER.error("cannot find teacher for teacherId: " + teacherId);
            return null;
        }
        teacher.currentAssaignedDoubt = "";
        save(teacher);
        return teacher;
    }
}
