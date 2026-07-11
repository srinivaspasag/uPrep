package com.vedantu.organization.daos.ei;

import org.bson.types.ObjectId;

import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.organization.models.ei.OrgUploadAttempt;

public class OrgUploadAttemptDAO extends VedantuBasicDAO<OrgUploadAttempt, ObjectId> {

    public static OrgUploadAttemptDAO INSTANCE = new OrgUploadAttemptDAO();

    private OrgUploadAttemptDAO() {

        super(OrgUploadAttempt.class);
    }

}
