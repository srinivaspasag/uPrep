package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.cmds.managers.CMDSModuleManager;
import com.vedantu.cmds.pojos.requests.slpmodules.AddModuleEntryReq;
import com.vedantu.cmds.pojos.requests.slpmodules.CreateModuleReq;
import com.vedantu.cmds.pojos.requests.slpmodules.DeleteModuleEntryReq;
import com.vedantu.cmds.pojos.requests.slpmodules.DeleteModuleReq;
import com.vedantu.cmds.pojos.requests.slpmodules.GetCMDSModulesReq;
import com.vedantu.cmds.pojos.requests.slpmodules.GetModuleInfoReq;
import com.vedantu.cmds.pojos.requests.slpmodules.MoveModuleEntryReq;
import com.vedantu.cmds.pojos.responses.AddModuleRes;
import com.vedantu.cmds.pojos.responses.slpmodules.CreateModuleRes;
import com.vedantu.cmds.pojos.responses.slpmodules.DeleteModuleEntryRes;
import com.vedantu.cmds.pojos.responses.slpmodules.DeleteModuleRes;
import com.vedantu.cmds.pojos.responses.slpmodules.GetCMDSModulesRes;
import com.vedantu.cmds.pojos.responses.slpmodules.GetModuleInfoRes;
import com.vedantu.cmds.pojos.responses.slpmodules.MoveModuleEntryRes;
import com.vedantu.cmds.pojos.responses.slpmodules.ScheduleRes;
import com.vedantu.cmds.pojos.responses.slpmodules.UpdateModuleEntryRes;
import com.vedantu.cmds.pojos.responses.slpmodules.UpdateModuleRes;
import com.vedantu.commons.JSONResponse;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.content.pojos.requests.ModuleScheduleReq;
import com.vedantu.content.pojos.requests.UpdateModuleEntryReq;
import com.vedantu.content.pojos.requests.UpdateModuleReq;

public class CMDSModules extends AbstractVedantuController{
    private static ALogger LOGGER = Logger.of(CMDSQuestions.class);



    public static Result createModule() {

        LOGGER.debug(" Called createModule");
        Form<CreateModuleReq> createModuleReqForm = Form.form(CreateModuleReq.class)
                .bindFromRequest();

        if (createModuleReqForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }

        CreateModuleReq createModuleReq = createModuleReqForm.get();
        CreateModuleRes createModuleResponse = null;

        try {
            createModuleResponse = CMDSModuleManager.createModule(createModuleReq);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(createModuleResponse).toObjectNode());
    }

    public static Result addModuleEntries() {

        LOGGER.debug(" Called addModuleEntries");
        Form<AddModuleEntryReq> addModuleReqForm = Form.form(AddModuleEntryReq.class)
                .bindFromRequest();

        if (addModuleReqForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }

        AddModuleEntryReq addModuleReq = addModuleReqForm.get();
        AddModuleRes addModuleResponse = null;

        try {
            addModuleResponse = CMDSModuleManager.addModuleEntries(addModuleReq);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(addModuleResponse).toObjectNode());
    }

    public static Result setSchedule(){
        Form<ModuleScheduleReq> setScheduleForm = Form.form(ModuleScheduleReq.class).bindFromRequest();
        if (setScheduleForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        ModuleScheduleReq setScheduleReq = setScheduleForm.get();
        ScheduleRes setScheduleRes = null;
        try{
            setScheduleRes = CMDSModuleManager.addSchedule(setScheduleReq);
        } catch(VedantuException e) {
            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(setScheduleRes).toObjectNode());
    }

    public static Result deleteSchedule(){
        Form<ModuleScheduleReq> deleteScheduleForm = Form.form(ModuleScheduleReq.class).bindFromRequest();
        if (deleteScheduleForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        ModuleScheduleReq deleteScheduleReq = deleteScheduleForm.get();
        ScheduleRes deleteScheduleRes = null;
        try{
            deleteScheduleRes = CMDSModuleManager.deleteSchedule(deleteScheduleReq);
        } catch(VedantuException e) {
            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(deleteScheduleRes).toObjectNode());
    }

    public static Result updateModuleEntries() {

        Form<UpdateModuleEntryReq> updateModuleEntryReqForm = Form.form(UpdateModuleEntryReq.class)
                .bindFromRequest();

        if (updateModuleEntryReqForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }

        UpdateModuleEntryReq updateModuleEntryReq = updateModuleEntryReqForm.get();
        UpdateModuleEntryRes updateModuleEntryResponse = null;

        try {
            updateModuleEntryResponse = CMDSModuleManager.updateModuleEntry(updateModuleEntryReq);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(updateModuleEntryResponse).toObjectNode());
    }


    public static Result getCMDSModuleInfo() {

        LOGGER.debug(" Called createModule");
        Form<GetModuleInfoReq> getModuleInfoReqForm = Form.form(GetModuleInfoReq.class)
                .bindFromRequest();

        if (getModuleInfoReqForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }

        GetModuleInfoReq getModuleInfoReq = getModuleInfoReqForm.get();
        GetModuleInfoRes getModuleInfoResponse = null;

        try {
            getModuleInfoResponse = CMDSModuleManager.getModuleInfo(getModuleInfoReq);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(getModuleInfoResponse).toObjectNode());
    }

    public static Result getCMDSModules() {

        LOGGER.debug(" Called createModule");
        Form<GetCMDSModulesReq> getCMDSModulesReqForm = Form.form(GetCMDSModulesReq.class)
                .bindFromRequest();

        if (getCMDSModulesReqForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }

        GetCMDSModulesReq getCMDSModulesReq = getCMDSModulesReqForm.get();
        GetCMDSModulesRes getCMDSModulesResponse = null;

        try {
            getCMDSModulesResponse = CMDSModuleManager.getCMDSModules(getCMDSModulesReq);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(getCMDSModulesResponse).toObjectNode());
    }


    public static Result moveModuleEntry() {

        LOGGER.debug(" Called createModule");
        Form<MoveModuleEntryReq> moveModuleEntryReqForm = Form.form(MoveModuleEntryReq.class)
                .bindFromRequest();

        if (moveModuleEntryReqForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }

        MoveModuleEntryReq moveModuleEntryReq = moveModuleEntryReqForm.get();
        MoveModuleEntryRes moveModuleEntryResponse = null;

        try {
            moveModuleEntryResponse = CMDSModuleManager.moveModuleEntry(moveModuleEntryReq);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(moveModuleEntryResponse).toObjectNode());
    }


    public static Result deleteModuleEntry() {

        Form<DeleteModuleEntryReq> deleteModuleEntryReqForm = Form.form(DeleteModuleEntryReq.class)
                .bindFromRequest();

        if (deleteModuleEntryReqForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }

        DeleteModuleEntryReq deleteModuleEntryReq = deleteModuleEntryReqForm.get();
        DeleteModuleEntryRes deleteModuleEntryResponse = null;

        try {
            deleteModuleEntryResponse = CMDSModuleManager.deleteModuleEntry(deleteModuleEntryReq);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(deleteModuleEntryResponse).toObjectNode());
    }

    public static Result deleteModule() {

        LOGGER.debug(" Called createModule");
        Form<DeleteModuleReq> deleteModuleReqForm = Form.form(DeleteModuleReq.class)
                .bindFromRequest();

        if (deleteModuleReqForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }

        DeleteModuleReq deleteModuleReq = deleteModuleReqForm.get();
        DeleteModuleRes deleteModuleResponse = null;

        try {
            deleteModuleResponse = CMDSModuleManager.deleteModule(deleteModuleReq);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(deleteModuleResponse).toObjectNode());
    }


    public static Result updateModule() {

        LOGGER.debug(" Called createModule");
        Form<UpdateModuleReq> updateModuleReqForm = Form.form(UpdateModuleReq.class)
                .bindFromRequest();

        if (updateModuleReqForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }

        UpdateModuleReq updateModuleReq = updateModuleReqForm.get();
        UpdateModuleRes updateModuleResponse = null;

        try {
            updateModuleResponse = CMDSModuleManager.updateModule(updateModuleReq);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(updateModuleResponse).toObjectNode());
    }




}
