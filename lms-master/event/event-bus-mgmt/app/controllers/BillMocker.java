package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.api.mvc.Result;

import com.vedantu.commons.pojos.responses.ActionTakenRes;
import com.vedantu.organization.daos.LicensingPlanDAO;
import com.vedantu.organization.pojos.requests.organizations.GetOrgReq;

public class BillMocker extends AbstractVedantuController {

    private static final ALogger LOGGER = Logger.of(LicensingPlanDAO.class);

    public Result generate() {

        LOGGER.debug(" Called createDirectory");

        GetOrgReq request = null;
        ActionTakenRes response = null;

        return null;

    }
}
