package controllers;

import java.util.List;

import org.json.JSONException;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;

import com.vedantu.cmds.managers.CMDSLibraryManager;
import com.vedantu.cmds.managers.CMDSResourcesManager;
import com.vedantu.cmds.pojos.requests.AddMappingsReq;
import com.vedantu.cmds.pojos.requests.CreateFolderReq;
import com.vedantu.cmds.pojos.requests.DeleteContentReq;
import com.vedantu.cmds.pojos.requests.DeleteMappingReq;
import com.vedantu.cmds.pojos.requests.GetFoldersReq;
import com.vedantu.cmds.pojos.requests.GetResourcesReq;
import com.vedantu.cmds.pojos.requests.GetSharedQuestionsBasicInfoReq;
import com.vedantu.cmds.pojos.requests.MoveContentReq;
import com.vedantu.cmds.pojos.requests.SaveMappingsReq;
import com.vedantu.cmds.pojos.requests.VisibleMappingReq;
import com.vedantu.cmds.pojos.requests.library.GetEntityPublishingStatusReq;
import com.vedantu.cmds.pojos.requests.library.PublishReq;
import com.vedantu.cmds.pojos.requests.videos.SignUploadFileReq;
import com.vedantu.cmds.pojos.requests.videos.UploadCMDSContentFileReq;
import com.vedantu.cmds.pojos.responses.AddMappingsRes;
import com.vedantu.cmds.pojos.responses.CreateFolderRes;
import com.vedantu.cmds.pojos.responses.DeleteContentRes;
import com.vedantu.cmds.pojos.responses.GetFoldersRes;
import com.vedantu.cmds.pojos.responses.GetResourcesRes;
import com.vedantu.cmds.pojos.responses.GetSharedQuestionsBasicInfoRes;
import com.vedantu.cmds.pojos.responses.MoveContentRes;
import com.vedantu.cmds.pojos.responses.SaveMappingRes;
import com.vedantu.cmds.pojos.responses.ShareMappingResponse;
import com.vedantu.cmds.pojos.responses.UploadContentFileRes;
import com.vedantu.cmds.pojos.responses.library.GetStatus;
import com.vedantu.cmds.pojos.responses.library.PublishRes;
import com.vedantu.cmds.pojos.responses.questions.EditContentRes;
import com.vedantu.commons.JSONResponse;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.fs.responses.SignUploadFileRes;
import com.vedantu.content.pojos.requests.EditContentReq;

public class CMDSResources extends AbstractVedantuController {

    private static final ALogger LOGGER = Logger.of(CMDSResources.class);

    /**
     * Getting resources for folder { {@link GetResourcesReq}
     *
     * @return { {@link GetResourcesRes }
     */
    public static Result getResources() {

        LOGGER.debug(" Called getResources");
        Form<GetResourcesReq> requestForm = Form.form(GetResourcesReq.class).bindFromRequest();

        if (requestForm.hasErrors()) {
            LOGGER.debug("Error in getResources" + requestForm.errors());
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetResourcesRes response = null;
        try {
            GetResourcesReq request = requestForm.get();
            response = CMDSResourcesManager.getResources(request);
        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }

        return ok(getResultResponse(response).toObjectNode());

    }

    public static Result getQuestionSharingBasicInfo() {
        LOGGER.debug(" Called getQuestionSharingBasicInfo");
        Form<GetSharedQuestionsBasicInfoReq> requestForm = Form.form(GetSharedQuestionsBasicInfoReq.class).bindFromRequest();

        if (requestForm.hasErrors()) {
            LOGGER.debug("Error in getResources" + requestForm.errors());
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetSharedQuestionsBasicInfoRes response = null;
        try {
            GetSharedQuestionsBasicInfoReq request = requestForm.get();
            response = CMDSResourcesManager.getQuestionSharingBasicInfo(request);
        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }

        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result addMappings() {
        LOGGER.debug(" Called addMappings");
        Form<AddMappingsReq> requestForm = Form.form(AddMappingsReq.class).bindFromRequest();

        if (requestForm.hasErrors()) {
            LOGGER.debug("Error in getResources" + requestForm.errors());
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        AddMappingsRes response = null;
        try {
            AddMappingsReq request = requestForm.get();
            response = CMDSResourcesManager.getBoardsToAddMappings(request);
        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }

        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result saveMapping() {
        LOGGER.debug(" Called addMappings");
        Form<SaveMappingsReq> requestForm = Form.form(SaveMappingsReq.class).bindFromRequest();

        if (requestForm.hasErrors()) {
            LOGGER.debug("Error in getResources" + requestForm.errors());
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        SaveMappingRes response = null;
        try {
            SaveMappingsReq request = requestForm.get();
            response = CMDSResourcesManager.saveBoardMapping(request);
        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }

        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result deleteMapping() {
        LOGGER.debug(" Called addMappings");
        Form<DeleteMappingReq> requestForm = Form.form(DeleteMappingReq.class).bindFromRequest();

        if (requestForm.hasErrors()) {
            LOGGER.debug("Error in getResources" + requestForm.errors());
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        SaveMappingRes response = null;
        try {
            DeleteMappingReq request = requestForm.get();
            response = CMDSResourcesManager.deleteBoardMapping(request);
        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }

        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result shareMapping() {
        LOGGER.debug(" Called addMappings");
        Form<DeleteMappingReq> requestForm = Form.form(DeleteMappingReq.class).bindFromRequest();

        if (requestForm.hasErrors()) {
            LOGGER.debug("Error in getResources" + requestForm.errors());
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        List<ShareMappingResponse> response = null;
        try {
            DeleteMappingReq request = requestForm.get();
            response = CMDSResourcesManager.shareBoardMapping(request);
        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            return ok((new JSONResponse(e)).toObjectNode());
        }

        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result visibleMapping() {
        LOGGER.debug(" Called addMappings");
        Form<VisibleMappingReq> requestForm = Form.form(VisibleMappingReq.class).bindFromRequest();

        if (requestForm.hasErrors()) {
            LOGGER.debug("Error in getResources" + requestForm.errors());
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        SaveMappingRes response = null;
        try {
            VisibleMappingReq request = requestForm.get();
            response = CMDSResourcesManager.visibleBoardMapping(request);
        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }

        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result getQuestionsCount() {

        LOGGER.debug(" Called getQuestionsCount");
        Form<GetResourcesReq> requestForm = Form.form(GetResourcesReq.class).bindFromRequest();

        if (requestForm.hasErrors()) {
            LOGGER.debug("Error in getQuestionsCount" + requestForm.errors());
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetResourcesRes response = null;
        try {
            GetResourcesReq request = requestForm.get();
            response = CMDSResourcesManager.getQuestionsCount(request);
        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }

        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result getQuestions() {

        LOGGER.debug(" Called getQuestions");
        Form<GetResourcesReq> requestForm = Form.form(GetResourcesReq.class).bindFromRequest();

        if (requestForm.hasErrors()) {
            LOGGER.debug("Error in getQuestions" + requestForm.errors());
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetResourcesRes response = null;
        try {
            GetResourcesReq request = requestForm.get();
            response = CMDSResourcesManager.getQuestions(request);
        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }

        return ok(getResultResponse(response).toObjectNode());
    }

    /**
     * Create folder Request {@link CreateFolderRes }
     *
     * @return { {@link CreateFolderRes}
     */

    public static Result createFolder() {

        LOGGER.debug(" Called createFolder");
        Form<CreateFolderReq> createFolderReqForm = Form.form(CreateFolderReq.class)
                .bindFromRequest();

        if (createFolderReqForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }

        CreateFolderReq createFolderReq = createFolderReqForm.get();
        CreateFolderRes createFolderResponse = null;

        try {
            createFolderResponse = CMDSResourcesManager.createFolder(createFolderReq);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(createFolderResponse).toObjectNode());
    }

    /**
     * Gives sub folders given folderId or returns root folder for organization
     * {@link GetFoldersReq} {@link GetFoldersRes}
     *
     * @return
     */

    public static Result getFolders() {

        LOGGER.debug(" Called getFolders");
        Form<GetFoldersReq> getFoldersReqForm = Form.form(GetFoldersReq.class).bindFromRequest();

        if (getFoldersReqForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getFoldersReqForm))).toObjectNode());
        }

        GetFoldersReq getFoldersReq = getFoldersReqForm.get();
        GetFoldersRes getFoldersRes = null;

        try {
            getFoldersRes = CMDSResourcesManager.getFolders(getFoldersReq);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(getFoldersRes).toObjectNode());
    }

    /**
     * Gives targetFolderId and entities to move , move will move entities except folder to its
     * child {@link MoveContentReq} {@link MoveContentRes}
     *
     * @return
     */
    public static Result move() {

        LOGGER.debug(" Called move to other folder");
        Form<MoveContentReq> moveContentReqForm = Form.form(MoveContentReq.class).bindFromRequest();

        if (moveContentReqForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(moveContentReqForm))).toObjectNode());
        }

        MoveContentReq moveContentReq = moveContentReqForm.get();
        MoveContentRes moveContentRes = null;

        try {
            moveContentRes = CMDSResourcesManager.moveFolder(moveContentReq);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(moveContentRes).toObjectNode());
    }

    /**
     * Requests for entities needs to be deleted child {@link DeleteContentReq}
     * {@link DeleteContentRes}
     *
     * @return
     */
    public static Result delete() {

        LOGGER.debug(" Called delete content");
        Form<DeleteContentReq> deleteContentReqForm = Form.form(DeleteContentReq.class)
                .bindFromRequest();

        if (deleteContentReqForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(deleteContentReqForm))).toObjectNode());
        }

        DeleteContentReq deleteRequest = deleteContentReqForm.get();
        DeleteContentRes deleteResponse = null;

        try {
            deleteResponse = CMDSResourcesManager.delete(deleteRequest);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(deleteResponse).toObjectNode());
    }

    public static Result publish() {

        LOGGER.debug(" Called publish");
        //

        Form<PublishReq> requestForm = Form.form(PublishReq.class).bindFromRequest();

        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        //
        PublishReq request = requestForm.get();
        PublishRes response = null;
        //
        try {

            // MutableLong totalHits = new MutableLong(0);
            response = CMDSLibraryManager.publish(request);
            //

        } catch (VedantuException ex) {
            Logger.debug("Error", ex);
            LOGGER.debug("Error", ex);
            return ok((new JSONResponse(new VedantuException(VedantuErrorCode.SERVICE_ERROR)))
                    .toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());

    }

    public static Result getStatus() {

        LOGGER.debug(" Called createDirectory");
        //

        Form<GetEntityPublishingStatusReq> requestForm = Form.form(
                GetEntityPublishingStatusReq.class).bindFromRequest();

        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        //
        GetEntityPublishingStatusReq request = requestForm.get();
        GetStatus response = null;
        //
        try {

            // MutableLong totalHits = new MutableLong(0);
            response = CMDSLibraryManager.getStatus(request);
            //

        } catch (VedantuException ex) {
            Logger.debug("Error", ex);
            LOGGER.debug("Error", ex);
            return ok((new JSONResponse(new VedantuException(VedantuErrorCode.SERVICE_ERROR)))
                    .toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());

    }

    /**
     * Will Sign request only using S3 not will sign in other cases
     *
     * @return
     * @throws VedantuException
     */
    public static Result getSignedRequest() throws VedantuException {

        SignUploadFileReq request = null;
        SignUploadFileRes response = null;

        try {
            Form<SignUploadFileReq> requestForm = Form.form(SignUploadFileReq.class)
                    .bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }

            request = requestForm.get();
            if (request.mediaType == MediaType.UNKNOWN) {
                throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
            }

            response = CMDSResourcesManager.sign(request);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    /**
     * Add solution to existing question {@link UploadCMDSContentFileReq} {@link SignUploadFileRes}
     *
     * @return
     */
    public static Result upload() {

        LOGGER.debug(" Called uploadVideo" + request().body());
        response().setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, request().getHeader(ORIGIN));

        MultipartFormData body = request().body().asMultipartFormData();
        UploadCMDSContentFileReq request = new UploadCMDSContentFileReq(body);
        UploadContentFileRes response = new UploadContentFileRes();
        response.success = false;
        try {

            if (request.validate() != null) {
                CMDSVideos.LOGGER.debug("Validation Error " + request.validate());
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }

            response.success = CMDSResourcesManager.upload(request);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        if (response.success) {
            LOGGER.debug("Upload successful");
            return ok(getResultResponse(response).toObjectNode());
        }

        return badRequest(getResultResponse(response).toObjectNode());

    }

    /**
     * Add solution to existing question {@link UploadCMDSContentFileReq} {@link SignUploadFileRes}
     *
     * @return
     */
    public static Result cors(String bucket) {

        LOGGER.debug(" Called cors testing" + request().body()
                + request().method().equals("OPTIONS"));
        request().method().equals("OPTIONS");
        response().setHeader(ACCESS_CONTROL_ALLOW_METHODS, "POST, GET, HEAD, PUT, DELETE, OPTIONS");
        response().setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, request().getHeader(ORIGIN));

        response().setHeader(ACCESS_CONTROL_ALLOW_HEADERS, "*");
        response().setHeader(ACCESS_CONTROL_MAX_AGE, "" + 36000);

        return ok();

    }

    /**
     * Edit existing question {@link EditContentReq}
     *
     * @return { {@link EditContentRes}
     */
    public static Result update() {

        LOGGER.debug(" Called createDirectory");

        EditContentReq request = null;
        EditContentRes response = new EditContentRes();

        try {
            Form<EditContentReq> requestForm = Form.form(EditContentReq.class).bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response.isUpdated = CMDSResourcesManager.INSTANCE.update(request);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }
}
