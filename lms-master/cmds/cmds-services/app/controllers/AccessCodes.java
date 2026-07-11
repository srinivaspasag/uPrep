package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.cmds.managers.AccessCodeManager;
import com.vedantu.cmds.models.AccessCode;
import com.vedantu.cmds.pojos.requests.accesscodes.GenerateAccessCodeReq;
import com.vedantu.cmds.pojos.requests.accesscodes.GenerateBulkAccessCodesReq;
import com.vedantu.cmds.pojos.requests.accesscodes.GetAccessCodesReq;
import com.vedantu.cmds.pojos.requests.accesscodes.GetSellableItemsReq;
import com.vedantu.cmds.pojos.requests.accesscodes.ManageDevicesReq;
import com.vedantu.cmds.pojos.requests.accesscodes.ResendEmailReq;
import com.vedantu.cmds.pojos.requests.accesscodes.UpdateShipmentStatusReq;
import com.vedantu.cmds.pojos.requests.accesscodes.VerifyAccessCodeReq;
import com.vedantu.cmds.pojos.responses.accessCodes.GenerateAccessCodeRes;
import com.vedantu.cmds.pojos.responses.accessCodes.GenerateBulkAccessCodesRes;
import com.vedantu.cmds.pojos.responses.accessCodes.GetAccessCodesRes;
import com.vedantu.cmds.pojos.responses.accessCodes.GetSellableItemsRes;
import com.vedantu.cmds.pojos.responses.accessCodes.ManageDevicesRes;
import com.vedantu.cmds.pojos.responses.accessCodes.ResendEmailRes;
import com.vedantu.cmds.pojos.responses.accessCodes.UpdateShipmentStatusRes;
import com.vedantu.cmds.pojos.responses.accessCodes.VerifyAccessCodeRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;

	public class AccessCodes extends AbstractVedantuController {

	    private static ALogger LOGGER = Logger.of(AccessCode.class);
	        	    
	    public static Result generateAccessCode() {

	        Form<GenerateAccessCodeReq> generateAccessCodeForm = Form.form(GenerateAccessCodeReq.class)
	                .bindFromRequest();
	        Logger.debug("request params : " + generateAccessCodeForm.data());
	        if (generateAccessCodeForm.hasErrors()) {
	            return ok(getErrorResponse(
	                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
	                            getErrorMessege(generateAccessCodeForm))).toObjectNode());
	        }
	        GenerateAccessCodeReq getAccessCodeReq = generateAccessCodeForm.get();
	        GenerateAccessCodeRes getAccessCodeRes = null;
	        try {
	        	getAccessCodeRes = AccessCodeManager.INSTANCE.generateAccessCode(getAccessCodeReq);
	        } catch (VedantuException e) {
	            return ok(getErrorResponse(e).toObjectNode());
	        }
	        return ok(getResultResponse(getAccessCodeRes).toObjectNode());
	    }

	    public static Result generateBulkAccessCode() {

	        Form<GenerateBulkAccessCodesReq> generateBulkAccessCodesForm = Form.form(GenerateBulkAccessCodesReq.class)
	                .bindFromRequest();
	        Logger.debug("request params : " + generateBulkAccessCodesForm.data());
	        if (generateBulkAccessCodesForm.hasErrors()) {
	            return ok(getErrorResponse(
	                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
	                            getErrorMessege(generateBulkAccessCodesForm))).toObjectNode());
	        }
	        GenerateBulkAccessCodesReq getBulkAccessCodesReq = generateBulkAccessCodesForm.get();
	        GenerateBulkAccessCodesRes getBulkAccessCodesRes = null;
	        try {
	            getBulkAccessCodesRes = AccessCodeManager.INSTANCE.generateBulkAccessCodes(getBulkAccessCodesReq);
	        } catch (VedantuException e) {
	            return ok(getErrorResponse(e).toObjectNode());
	        }
	        return ok(getResultResponse(getBulkAccessCodesRes).toObjectNode());
	    }
	    
	    public static Result verifyAccessCode() {

	        Form<VerifyAccessCodeReq> verifyAccessCodeForm = Form.form(VerifyAccessCodeReq.class)
	                .bindFromRequest();
	        Logger.debug("request params : " + verifyAccessCodeForm.data());
	        if (verifyAccessCodeForm.hasErrors()) {
	            return ok(getErrorResponse(
	                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
	                            getErrorMessege(verifyAccessCodeForm))).toObjectNode());
	        }
	        VerifyAccessCodeReq verifyAccessCodeReq = verifyAccessCodeForm.get();
	        VerifyAccessCodeRes verifyAccessCodeRes = null;
	        try {
	        	verifyAccessCodeRes = AccessCodeManager.INSTANCE.verifyAccessCode(verifyAccessCodeReq);
	        } catch (VedantuException e) {
	            return ok(getErrorResponse(e).toObjectNode());
	        }
	        return ok(getResultResponse(verifyAccessCodeRes).toObjectNode());
	    }
	    
	    public static Result deviceManagement() {

	        Form<ManageDevicesReq> manageDevicesForm = Form.form(ManageDevicesReq.class)
	                .bindFromRequest();
	        Logger.debug("request params : " + manageDevicesForm.data());
	        if (manageDevicesForm.hasErrors()) {
	            return ok(getErrorResponse(
	                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
	                            getErrorMessege(manageDevicesForm))).toObjectNode());
	        }
	        ManageDevicesReq manageDevicesReq = manageDevicesForm.get();
	        ManageDevicesRes manageDevicesRes = null;
	        try {
	        	manageDevicesRes = AccessCodeManager.INSTANCE.deviceManagement(manageDevicesReq);
	        } catch (VedantuException e) {
	            return ok(getErrorResponse(e).toObjectNode());
	        }
	        return ok(getResultResponse(manageDevicesRes).toObjectNode());
	    }
	    
	    public static Result getAccessCodes() {

	        Form<GetAccessCodesReq> getAccessCodesForm = Form.form(GetAccessCodesReq.class)
	                .bindFromRequest();
	        Logger.debug("request params : " + getAccessCodesForm.data());
	        if (getAccessCodesForm.hasErrors()) {
	            return ok(getErrorResponse(
	                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
	                            getErrorMessege(getAccessCodesForm))).toObjectNode());
	        }
	        GetAccessCodesReq getAccessCodesReq = getAccessCodesForm.get();
	        GetAccessCodesRes getAccessCodesRes = null;
	        try {
	        	getAccessCodesRes = AccessCodeManager.INSTANCE.getAccessCodes(getAccessCodesReq);
	        } catch (VedantuException e) {
	            return ok(getErrorResponse(e).toObjectNode());
	        }
	        return ok(getResultResponse(getAccessCodesRes).toObjectNode());
	    }
	    
	    public static Result getSellableItems() {

	        Form<GetSellableItemsReq> getSellableItemsForm = Form.form(GetSellableItemsReq.class)
	                .bindFromRequest();
	        Logger.debug("request params : " + getSellableItemsForm.data());
	        if (getSellableItemsForm.hasErrors()) {
	            return ok(getErrorResponse(
	                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
	                            getErrorMessege(getSellableItemsForm))).toObjectNode());
	        }
	        GetSellableItemsReq getSellableItemsReq = getSellableItemsForm.get();
	        GetSellableItemsRes getSellableItemsRes = null;
	        try {
	        	getSellableItemsRes = AccessCodeManager.INSTANCE.getSellableItems(getSellableItemsReq);
	        } catch (VedantuException e) {
	            return ok(getErrorResponse(e).toObjectNode());
	        }
	        return ok(getResultResponse(getSellableItemsRes).toObjectNode());
	    }
	    
	    public static Result resendEmail() {

	        Form<ResendEmailReq> resendEmailReqForm = Form.form(ResendEmailReq.class)
	                .bindFromRequest();
	        Logger.debug("request params : " + resendEmailReqForm.data());
	        if (resendEmailReqForm.hasErrors()) {
	            return ok(getErrorResponse(
	                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
	                            getErrorMessege(resendEmailReqForm))).toObjectNode());
	        }
	        ResendEmailReq resendEmailReq = resendEmailReqForm.get();
	        ResendEmailRes resendEmailRes = null;
	        try {
	        	resendEmailRes = AccessCodeManager.INSTANCE.resendEmail(resendEmailReq);
	        } catch (VedantuException e) {
	            return ok(getErrorResponse(e).toObjectNode());
	        }
	        return ok(getResultResponse(resendEmailRes).toObjectNode());
	    }
	    
	    public static Result updateShipmentStatus() {

	        Form<UpdateShipmentStatusReq> updateShipmentStatusReqForm = Form.form(UpdateShipmentStatusReq.class)
	                .bindFromRequest();
	        Logger.debug("request params : " + updateShipmentStatusReqForm.data());
	        if (updateShipmentStatusReqForm.hasErrors()) {
	            return ok(getErrorResponse(
	                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
	                            getErrorMessege(updateShipmentStatusReqForm))).toObjectNode());
	        }
	        UpdateShipmentStatusReq updateShipmentStatusReq = updateShipmentStatusReqForm.get();
	        UpdateShipmentStatusRes updateShipmentStatusRes = null;
	        try {
	        	updateShipmentStatusRes = AccessCodeManager.INSTANCE.updateShipmentStatus(updateShipmentStatusReq);
	        } catch (VedantuException e) {
	            return ok(getErrorResponse(e).toObjectNode());
	        }
	        return ok(getResultResponse(updateShipmentStatusRes).toObjectNode());
	    }
	
}
