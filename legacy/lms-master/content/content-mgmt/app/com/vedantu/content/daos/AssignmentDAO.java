package com.vedantu.content.daos;

import java.util.List;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.enums.EnumBasket.TestType;
import com.vedantu.content.enums.TestMode;
import com.vedantu.content.enums.TestResultVisibility;
import com.vedantu.content.models.Video;
import com.vedantu.content.models.tests.Assignment;
import com.vedantu.content.pojos.tests.TestMetadata;

public class AssignmentDAO extends AbstractAttemptableDAO<Assignment, ObjectId> {

    private static final ALogger      LOGGER   = Logger.of(AssignmentDAO.class);
    public static final AssignmentDAO INSTANCE = new AssignmentDAO();

    private AssignmentDAO() {

        super(Assignment.class);
    }

    public Assignment addAssignment(String userId, String name, String code, String desc,
            int qusCount, long duration, int totalMarks, List<TestMetadata> metadata,
            TestType type, TestMode mode, SrcEntity contentSrc, Scope scope,
            TestResultVisibility resultVisibility) {

        Assignment assignment = new Assignment(userId, name, desc, qusCount, duration, totalMarks,
                metadata, type, mode, code, scope, resultVisibility);
        assignment.contentSrc = contentSrc;
        save(assignment);
        return assignment;
    }

    public Assignment getAssignment(String id) throws VedantuException {

        Assignment assignment = getById(id);
        if (assignment == null) {
            LOGGER.error("no assignment found with id: " + id);
            throw new VedantuException(VedantuErrorCode.ASSIGNMENT_NOT_FOUND,
                    "no assignment found with id: " + id);
        }
        return assignment;
    }

    public Assignment getByCMDSAssignmentId(String id) {
        Query<Assignment> query = getQuery().filter("cmdsId", id);
        Assignment assignment = findOne(query);
        return assignment;
    }
}
