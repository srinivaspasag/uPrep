package com.vedantu.content.commons.daos;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.content.models.tests.Assignment;
import com.vedantu.mongo.VedantuBasicDAO;

public class AssignmentDAO extends VedantuBasicDAO<Assignment, ObjectId>{

	private static final ALogger            LOGGER   = Logger.of(AssignmentDAO.class);

    public static final AssignmentDAO INSTANCE = new AssignmentDAO();

    private AssignmentDAO() {
        super(Assignment.class);
    }
}
