package controllers;

import play.data.Form;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.content.managers.ChannelManager;
import com.vedantu.content.pojos.requests.channels.AddChannelReq;
import com.vedantu.content.pojos.requests.channels.AddContentToChannelReq;
import com.vedantu.content.pojos.requests.channels.EditChannelReq;
import com.vedantu.content.pojos.requests.channels.GetChannelsReq;
import com.vedantu.content.pojos.responses.channels.AddChannelRes;
import com.vedantu.content.pojos.responses.channels.AddContentToChannelRes;
import com.vedantu.content.pojos.responses.channels.GetChannelRes;

public class Channels extends AbstractVedantuController {

    public static Result addChannel() {

        Form<AddChannelReq> addChannelForm = Form.form(AddChannelReq.class).bindFromRequest();
        if (addChannelForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        AddChannelReq addChannelReq = addChannelForm.get();
        AddChannelRes addChannelRes = null;
        try {
            addChannelRes = ChannelManager.addChannel(addChannelReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(addChannelRes).toObjectNode());
    }

    public static Result getChannels() {

        Form<GetChannelsReq> getChannelsForm = Form.form(GetChannelsReq.class).bindFromRequest();
        if (getChannelsForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetChannelsReq getChannelsReq = getChannelsForm.get();
        ListResponse<GetChannelRes> getChannelsRes = null;
        try {
            getChannelsRes = ChannelManager.getChannels(getChannelsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getChannelsRes).toObjectNode());
    }

    /**
     * editChannel api can also be used for publishing the channel (scope need to be updated to
     * PUBLIC)
     * 
     * @return
     */
    public static Result editChannel() {

        Form<EditChannelReq> editChannelForm = Form.form(EditChannelReq.class).bindFromRequest();
        if (editChannelForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(editChannelForm))).toObjectNode());
        }
        EditChannelReq editChannelReq = editChannelForm.get();
        AddChannelRes editChannelRes = null;
        try {
            editChannelRes = ChannelManager.updateChannel(editChannelReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(editChannelRes).toObjectNode());
    }

    public static Result addContentToChannel() {

        Form<AddContentToChannelReq> addContentToChannelForm = Form.form(
                AddContentToChannelReq.class).bindFromRequest();
        if (addContentToChannelForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        AddContentToChannelReq addContentToChannelReq = addContentToChannelForm.get();
        AddContentToChannelRes addContentToChannelRes = null;
        try {
            addContentToChannelRes = ChannelManager.addContentToChannel(addContentToChannelReq,
                    false);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(addContentToChannelRes).toObjectNode());
    }

}
