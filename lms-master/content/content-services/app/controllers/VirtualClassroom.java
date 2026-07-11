package controllers;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.content.managers.VirtualClassroomManager;
import com.vedantu.content.pojos.requests.virtualclassroom.CreateRoomReq;
import com.vedantu.content.pojos.responses.virtualclassroom.CreateRoomRes;

import play.data.Form;
import play.mvc.Result;

public class VirtualClassroom extends AbstractVedantuController {
    public static Result createRoom(){
        Form<CreateRoomReq> createRoomReqForm = Form.form(CreateRoomReq.class)
                .bindFromRequest();
        if (createRoomReqForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(createRoomReqForm))).toObjectNode());
        }
        CreateRoomReq createRoomReq = createRoomReqForm.get();
        CreateRoomRes createRoomRes = null;
        try {
            createRoomRes = VirtualClassroomManager.createClassroom(createRoomReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(createRoomRes).toObjectNode());
    }
}
