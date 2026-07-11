package controllers;

import play.data.Form;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.responses.SearchListResponse;
import com.vedantu.content.managers.AssignmentManager;
import com.vedantu.content.pojos.requests.tests.GetAssignmentDetailsReq;
import com.vedantu.content.pojos.requests.tests.GetAssignmentInfoReq;
import com.vedantu.content.pojos.requests.tests.GetAssignmentsReq;
import com.vedantu.content.pojos.requests.tests.GetTestDetailsReq;
import com.vedantu.content.pojos.responses.tests.GetAssignmentInfoRes;
import com.vedantu.content.pojos.responses.tests.GetAssignmentQuestionsRes;

public class Assignments extends AbstractVedantuController {

    public static Result getAssignmentInfo() {

        Form<GetAssignmentInfoReq> getAssignmentForm = Form.form(GetAssignmentInfoReq.class)
                .bindFromRequest();
        if (getAssignmentForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getAssignmentForm))).toObjectNode());
        }
        GetAssignmentInfoReq getAssignmentReq = getAssignmentForm.get();
        GetAssignmentInfoRes getAssignmentRes = null;
        try {
            getAssignmentRes = AssignmentManager.getAssignmentInfo(getAssignmentReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getAssignmentRes).toObjectNode());
    }

    /**
     * @return board wise questions for the assignment
     */
    public static Result getAssignmentQuestions() {

        Form<GetTestDetailsReq> getAssignmentDetailForm = Form.form(
                GetTestDetailsReq.class).bindFromRequest();
        if (getAssignmentDetailForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getAssignmentDetailForm))).toObjectNode());
        }
        GetTestDetailsReq getAssignmentDetailReq = getAssignmentDetailForm.get();
        GetAssignmentQuestionsRes getAssignmentDetailRes = null;
        try {
            getAssignmentDetailRes = AssignmentManager
                    .getAssignmentQuestions(getAssignmentDetailReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getAssignmentDetailRes).toObjectNode());
    }

    public static Result getAssignments() {

        Form<GetAssignmentsReq> getAssignmentsForm = Form.form(GetAssignmentsReq.class)
                .bindFromRequest();
        if (getAssignmentsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getAssignmentsForm))).toObjectNode());
        }
        GetAssignmentsReq getAssignmentsReq = getAssignmentsForm.get();
        SearchListResponse<GetAssignmentInfoRes> getAssignmentsRes = null;
        try {
            getAssignmentsRes = AssignmentManager.getAssignments(getAssignmentsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getAssignmentsRes).toObjectNode());
    }
}
