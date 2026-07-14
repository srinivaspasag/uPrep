package com.vedantu.organization.daos.ei;

import org.bson.types.ObjectId;

import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.organization.models.ei.OrgUploadAttemptRequestStatus;

public class OrgUploadAttemptRequestStatusDAO extends
        VedantuBasicDAO<OrgUploadAttemptRequestStatus, ObjectId> {

    public static OrgUploadAttemptRequestStatusDAO INSTANCE = new OrgUploadAttemptRequestStatusDAO();

    private OrgUploadAttemptRequestStatusDAO() {

        super(OrgUploadAttemptRequestStatus.class);

    }

}
