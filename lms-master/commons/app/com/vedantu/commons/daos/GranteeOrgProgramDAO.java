package com.vedantu.commons.daos;

import java.util.List;

import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.mongo.models.GranteeOrgProgram;

public class GranteeOrgProgramDAO extends
		VedantuBasicDAO<GranteeOrgProgram, ObjectId> {

	private static final ALogger LOGGER = Logger.of(GranteeOrgProgramDAO.class);

	public static final GranteeOrgProgramDAO INSTANCE = new GranteeOrgProgramDAO();

	private GranteeOrgProgramDAO() {
		super(GranteeOrgProgram.class);
	}

	public List<GranteeOrgProgram> getProgramsGrantedToMe(String providerOrgId, String departmentId,
			MutableLong totalHits) {
		LOGGER.debug("getGrateeOrgPrograms orgId: " + providerOrgId
				+ ", departmentId: " + departmentId);

		Query<GranteeOrgProgram> query = getQuery().filter("subscriberOrgId", providerOrgId);
		query.filter("recordState", VedantuRecordState.ACTIVE);
		query.order("cName");
		if (null != departmentId) {
			query.filter("departmentId", departmentId);
		}
		LOGGER.debug("getGrateeOrgPrograms query: " + query);

		List<GranteeOrgProgram> programs = query.asList();
		totalHits.setValue(query.countAll());

		LOGGER.info("getGrateeOrgPrograms totalHits: " + totalHits.getValue());

		return programs;
	}

	public MutableLong getProgramsCountGrantedToMe(String subsciberOrgId) {

		Query<GranteeOrgProgram> query = getQuery().filter("subscriberOrgId", subsciberOrgId);
		query.filter("recordState", VedantuRecordState.ACTIVE);
		List<GranteeOrgProgram> programs = query.asList();
		MutableLong totalHits=new MutableLong(0L);
		totalHits.setValue(query.countAll());
		LOGGER.info("getGrateeOrgPrograms totalHits: " + totalHits.getValue());

		return totalHits;
	}

	public List<GranteeOrgProgram> getGranteeOrgPrograms(String providerOrgId, String departmentId,
			MutableLong totalHits) {
		LOGGER.debug("getGrateeOrgPrograms orgId: " + providerOrgId
				+ ", departmentId: " + departmentId);

		Query<GranteeOrgProgram> query = getQuery().filter("subscriberOrgId", providerOrgId);
		query.filter("recordState", VedantuRecordState.ACTIVE);
		query.order("cName");
		if (null != departmentId) {
			query.filter("departmentId", departmentId);
		}
		LOGGER.debug("getGrateeOrgPrograms query: " + query);

		List<GranteeOrgProgram> programs = query.asList();
		totalHits.setValue(query.countAll());

		LOGGER.info("getGrateeOrgPrograms"+programs.size()+" totalHits: " + totalHits.getValue());

		return programs;
	}

	public List<GranteeOrgProgram> getGranteeOrgPrograms(String subscriberOrgId) {
        LOGGER.debug("getGrateeOrgPrograms subscriberOrgId: " + subscriberOrgId);

        Query<GranteeOrgProgram> query = getQuery().filter("subscriberOrgId", subscriberOrgId);
//        query.filter("providerOrgId", providerOrgId);
        query.filter("recordState", VedantuRecordState.ACTIVE);
        //query.order("cName");
        LOGGER.debug("getGrateeOrgPrograms query: " + query);

        List<GranteeOrgProgram> programs = query.asList();

        LOGGER.info("getGrateeOrgPrograms"+programs.size());

        return programs;
    }

	public List<GranteeOrgProgram> getGranteeOrgByProgId(String providerOrgId, String programId,
			MutableLong totalHits) {
		LOGGER.debug("getGranteeOrgByProgId orgId: " + providerOrgId
				+ ", programId: " + programId);

		Query<GranteeOrgProgram> query = getQuery().filter("providerOrgId", providerOrgId);
		query.order("cName");
		query.filter("recordState", VedantuRecordState.ACTIVE);
		if (null != programId) {
			query.filter("programId", programId);
		}
		LOGGER.debug("getGrateeOrgPrograms query: " + query);

		List<GranteeOrgProgram> programs = query.asList();
		totalHits.setValue(query.countAll());

		LOGGER.info("getGrateeOrgPrograms"+programs.size()+" totalHits: " + totalHits.getValue());

		return programs;
	}

	public GranteeOrgProgram removeProgramSharing(String providerOrgId, String programId,
            String subscriberOrgId) {

        Query<GranteeOrgProgram> query = getDS().find(entityClazz).filter("providerOrgId", providerOrgId);
        query.filter("programId", programId);
        query.filter("subscriberOrgId", subscriberOrgId);

        GranteeOrgProgram program = query.get();
        if (program != null) {
            program.recordState = VedantuRecordState.DELETED;
        }
        save(program);
        return program;
    }

    public GranteeOrgProgram addGranteeOrgProgram(String providerOrgId, String subscriberOrgId,
            String programId) throws VedantuException {

        LOGGER.debug("addGranteeOrgProgram providerOrgId: " + providerOrgId + ",subscriberOrgId: "
                + subscriberOrgId + ", programId: " + programId);

        GranteeOrgProgram orgProgram = getQuery().filter("providerOrgId", providerOrgId)
                .filter("subscriberOrgId", subscriberOrgId).filter("programId", programId)
                .order("cName").get();

        if (null != orgProgram) {
            if (orgProgram.recordState.equals(VedantuRecordState.ACTIVE)) {
                LOGGER.error("cannot add orgProgram as orgProgram already exists for orgId: "
                        + providerOrgId + ", subscriberOrgId: " + subscriberOrgId + ", programId: "
                        + programId);
                throw new VedantuException(VedantuErrorCode.ORGANIZATION_PROGRAM_ALREADY_EXISTS);
            }
            orgProgram.recordState = VedantuRecordState.ACTIVE;
        } else {
            orgProgram = new GranteeOrgProgram(providerOrgId, subscriberOrgId, programId);
        }
        save(orgProgram);
        return orgProgram;
    }
}
