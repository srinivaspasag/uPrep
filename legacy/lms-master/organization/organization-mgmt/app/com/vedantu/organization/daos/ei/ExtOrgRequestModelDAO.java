package com.vedantu.organization.daos.ei;

import org.bson.types.ObjectId;

import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.organization.models.ei.ExtOrgRequestModel;

public class ExtOrgRequestModelDAO extends VedantuBasicDAO<ExtOrgRequestModel, ObjectId> {

    public static ExtOrgRequestModelDAO INSTANCE = new ExtOrgRequestModelDAO();

    private ExtOrgRequestModelDAO() {

        super(ExtOrgRequestModel.class);

    }
}
