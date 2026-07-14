package controllers;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.content.managers.ContentManager;
import com.vedantu.content.pojos.requests.schedules.AddScheduleReq;
import com.vedantu.content.pojos.requests.schedules.GetScheduleReq;
import com.vedantu.content.pojos.requests.schedules.RemoveScheduleReq;
import com.vedantu.content.pojos.responses.schedule.GetDayScheduleRes;
import com.vedantu.content.pojos.responses.schedule.GetScheduleRes;
import com.vedantu.content.pojos.responses.schedule.SaveScheduleRes;

import play.data.Form;
import play.mvc.Result;

public class ClassroomConnect extends AbstractVedantuController {

    public static Result getSchedule() {

        Form<GetScheduleReq> getScheduleForm = Form.form(GetScheduleReq.class)
                .bindFromRequest();
        if (getScheduleForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getScheduleForm))).toObjectNode());
        }
        GetScheduleReq getScheduleReq = getScheduleForm.get();
        GetScheduleRes getScheduleRes = null;
        getScheduleRes = ContentManager.getSchedule(getScheduleReq);
        return ok(getResultResponse(getScheduleRes).toObjectNode());
    }

    public static Result getDaySchedule() {

        Form<GetScheduleReq> getScheduleForm = Form.form(GetScheduleReq.class)
                .bindFromRequest();
        if (getScheduleForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getScheduleForm))).toObjectNode());
        }
        GetScheduleReq getScheduleReq = getScheduleForm.get();
        GetDayScheduleRes getScheduleRes = null;
        try {
        getScheduleRes = ContentManager.getDaySchedule(getScheduleReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getScheduleRes).toObjectNode());
    }

    public static Result addSchedule() {

        Form<AddScheduleReq> addScheduleForm = Form.form(AddScheduleReq.class)
                .bindFromRequest();
        if (addScheduleForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(addScheduleForm))).toObjectNode());
        }
        AddScheduleReq addScheduleReq = addScheduleForm.get();
        SaveScheduleRes response = null;
        try {
            response = ContentManager.addSchedule(addScheduleReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result removeDaySchedule() {

        Form<GetScheduleReq> removeScheduleForm = Form.form(GetScheduleReq.class)
                .bindFromRequest();
        if (removeScheduleForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(removeScheduleForm))).toObjectNode());
        }
        GetScheduleReq removeScheduleReq = removeScheduleForm.get();
        SaveScheduleRes removeContentLinksRes = null;
        try {
            removeContentLinksRes = ContentManager.removeDaySchedule(removeScheduleReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(removeContentLinksRes).toObjectNode());
    }

    public static Result removeSchedule() {

        Form<RemoveScheduleReq> removeScheduleForm = Form.form(RemoveScheduleReq.class)
                .bindFromRequest();
        if (removeScheduleForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(removeScheduleForm))).toObjectNode());
        }
        RemoveScheduleReq removeScheduleReq = removeScheduleForm.get();
        SaveScheduleRes removeContentLinksRes = null;
        try {
            removeContentLinksRes = ContentManager.removeSchedule(removeScheduleReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(removeContentLinksRes).toObjectNode());
    }
}
