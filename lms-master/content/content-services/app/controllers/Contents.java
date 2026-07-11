package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.DownloadableFileInfo;
import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.content.managers.ContentManager;
import com.vedantu.content.models.ContentSearchDetails;
import com.vedantu.content.pojos.requests.AddEntityInfoReq;
import com.vedantu.content.pojos.requests.GetContentDownloadLinkReq;
import com.vedantu.content.pojos.requests.GetContentSecuredDownloadLinkReq;
import com.vedantu.content.pojos.requests.GetContentStateReq;
import com.vedantu.content.pojos.requests.GetContentsLinkReq;
import com.vedantu.content.pojos.requests.GetContentsReq;
import com.vedantu.content.pojos.requests.GetDownloadUrlOfPdfReq;
import com.vedantu.content.pojos.requests.GetEntityReq;
import com.vedantu.content.pojos.requests.GetEntityInfoForAppReq;
import com.vedantu.content.pojos.requests.GetEntityReviewsReq;
import com.vedantu.content.pojos.requests.ReIndexContentReq;
import com.vedantu.content.pojos.requests.file.GetFileInfoReq;
import com.vedantu.content.pojos.responses.GetCMDSEntityInfoRes;
import com.vedantu.content.pojos.responses.GetContentDownloadLinkRes;
import com.vedantu.content.pojos.responses.GetContentLinkRes;
import com.vedantu.content.pojos.responses.GetContentLinksRes;
import com.vedantu.content.pojos.responses.GetContentStateRes;
import com.vedantu.content.pojos.responses.GetDownloadUrlOfPdfRes;
import com.vedantu.content.pojos.responses.GetEntityReviewsRes;
import com.vedantu.content.pojos.responses.GetFileInfosRes;
import com.vedantu.content.pojos.responses.GetEntityInfoForAppRes;

public class Contents extends AbstractVedantuController {

    private static final ALogger LOGGER = Logger.of(Contents.class);

    public static Result getContentLinks() {

        Form<GetContentsLinkReq> getContentsForm = Form.form(GetContentsLinkReq.class)
                .bindFromRequest();
        if (getContentsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getContentsForm))).toObjectNode());
        }
        GetContentsLinkReq getContentLinksReq = getContentsForm.get();
        GetContentLinksRes getContentLinksRes = null;
        try {
            getContentLinksRes = ContentManager.getContentLinks(getContentLinksReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getContentLinksRes).toObjectNode());
    }
    
    public static Result checkWhetherProgramIsCompleted(){
    	
    	Form<GetContentStateReq> getContentsStateForm = Form.form(GetContentStateReq.class)
                .bindFromRequest();
    	 if (getContentsStateForm.hasErrors()) {
             return ok(getErrorResponse(
                     new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                             getErrorMessege(getContentsStateForm))).toObjectNode());
         }
    	 GetContentStateReq getContentStateReq = getContentsStateForm.get();
    	 GetContentStateRes getContentStateRes = null;
    	 LOGGER.info("getContentStateReq "+getContentStateReq.userId+" , "+getContentStateReq.sectionId);
         
         try {
        	 getContentStateRes = ContentManager.checkWhetherProgramIsCompleted(getContentStateReq);
         } catch (VedantuException e) {
             return ok(getErrorResponse(e).toObjectNode());
         }
		return ok(getResultResponse(getContentStateRes).toObjectNode());
    	
    }

    public static Result getRemovedContentLinks() {

        Form<GetContentsLinkReq> getRemovedContentsForm = Form.form(GetContentsLinkReq.class)
                .bindFromRequest();
        if (getRemovedContentsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getRemovedContentsForm))).toObjectNode());
        }
        GetContentsLinkReq getRemovedContentLinksReq = getRemovedContentsForm.get();
        ListResponse<GetContentLinkRes> getRemovedContentLinksRes = null;
        try {
            getRemovedContentLinksRes = ContentManager
                    .getRemovedContentLinks(getRemovedContentLinksReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getRemovedContentLinksRes).toObjectNode());
    }

    public static Result getContentDownloadLink() {

        Form<GetContentDownloadLinkReq> getContentDownloadUrlForm = Form.form(
                GetContentDownloadLinkReq.class).bindFromRequest();
        if (getContentDownloadUrlForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getContentDownloadUrlForm))).toObjectNode());
        }
        GetContentDownloadLinkReq getContentDownloadUrlReq = getContentDownloadUrlForm.get();
        GetContentDownloadLinkRes getContentDownloadUrlRes = null;
        try {
            getContentDownloadUrlRes = ContentManager
                    .getContentDownloadLink(getContentDownloadUrlReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getContentDownloadUrlRes).toObjectNode());
    }

    public static Result getPdfDownloadLink() {

        Form<GetDownloadUrlOfPdfReq> getPdfDownloadUrlForm = Form.form(
                GetDownloadUrlOfPdfReq.class).bindFromRequest();
        if (getPdfDownloadUrlForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getPdfDownloadUrlForm))).toObjectNode());
        }
        GetDownloadUrlOfPdfReq getPdfDownloadUrlReq = getPdfDownloadUrlForm.get();
        GetDownloadUrlOfPdfRes getPdfDownloadUrlRes = null;
        try {
            getPdfDownloadUrlRes = ContentManager
                    .getPdfUrl(getPdfDownloadUrlReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getPdfDownloadUrlRes).toObjectNode());
    }

    public static Result getFileInfos() {

        Form<GetFileInfoReq> getContentDownloadUrlForm = Form.form(GetFileInfoReq.class)
                .bindFromRequest();
        if (getContentDownloadUrlForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getContentDownloadUrlForm))).toObjectNode());
        }
        GetFileInfoReq getContentFileReq = getContentDownloadUrlForm.get();
        GetFileInfosRes getContentFileRes = null;
        try {
            getContentFileRes = ContentManager.getFileInfo(getContentFileReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getContentFileRes).toObjectNode());
    }

    public static Result getSecuredLink() {

        Form<GetContentSecuredDownloadLinkReq> getContentDownloadUrlForm = Form.form(
                GetContentSecuredDownloadLinkReq.class).bindFromRequest();
        if (getContentDownloadUrlForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getContentDownloadUrlForm))).toObjectNode());
        }
        GetContentSecuredDownloadLinkReq getContentDownloadUrlReq = getContentDownloadUrlForm.get();
        DownloadableFileInfo getContentDownloadUrlRes = null;
        try {
            getContentDownloadUrlRes = ContentManager.getSecureLink(getContentDownloadUrlReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getContentDownloadUrlRes).toObjectNode());
    }

    public static Result getContents() {

        Form<GetContentsReq> getContentsForm = Form.form(GetContentsReq.class).bindFromRequest();
        if (getContentsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getContentsForm))).toObjectNode());
        }
        GetContentsReq getQuestionListReq = getContentsForm.get();
        ListResponse<ContentSearchDetails> getContentsRes = ContentManager
                .getContents(getQuestionListReq);
        return ok(getResultResponse(getContentsRes).toObjectNode());
    }

    public static Result getEntityInfoForApp() {

        Form<GetEntityInfoForAppReq> getInfoForm = Form.form(GetEntityInfoForAppReq.class).bindFromRequest();
        if (getInfoForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getInfoForm))).toObjectNode());
        }
        GetEntityInfoForAppReq getInfoReq = getInfoForm.get();
        LOGGER.info("requested getEntityInfoForApp of "+getInfoReq.entity.type+" id :" + getInfoReq.entity.id);
        GetEntityInfoForAppRes response = null;
        try {
            response = ContentManager.getEntityInfoForApp(getInfoReq);
        } catch (VedantuException e){
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result getCMDSEntityInfo() {

        Form<GetEntityInfoForAppReq> getInfoForm = Form.form(GetEntityInfoForAppReq.class).bindFromRequest();
        if (getInfoForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getInfoForm))).toObjectNode());
        }
        GetEntityInfoForAppReq getInfoReq = getInfoForm.get();
        LOGGER.info("requested getCMDSEntityInfo of "+getInfoReq.entity.type+" id :" + getInfoReq.entity.id);
        GetCMDSEntityInfoRes response = null;
        try {
            response = ContentManager.getCMDSEntityInfo(getInfoReq);
        } catch (VedantuException e){
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result getEntityReviews() {

        Form<GetEntityReviewsReq> getInfoForm = Form.form(GetEntityReviewsReq.class).bindFromRequest();
        if (getInfoForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getInfoForm))).toObjectNode());
        }
        GetEntityReviewsReq getInfoReq = getInfoForm.get();
        LOGGER.info("requested getEntityReviews of "+getInfoReq.entity.type+" id :" + getInfoReq.entity.id);
        GetEntityReviewsRes response = null;
        try {
            response = ContentManager.getEntityReviews(getInfoReq);
        } catch (VedantuException e){
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result reIndexContents() {

        Form<ReIndexContentReq> reIndexContentsForm = Form.form(ReIndexContentReq.class)
                .bindFromRequest();
        if (reIndexContentsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(reIndexContentsForm))).toObjectNode());
        }
        ReIndexContentReq reIndexContentReq = reIndexContentsForm.get();
        boolean reIndexContentRes = false;
        try {
            reIndexContentRes = ContentManager.reIndexLibraryContents(reIndexContentReq.entityType,
                    reIndexContentReq.ids);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(reIndexContentRes).toObjectNode());
    }

    public static Result reIndexContentLinks() {

        Form<ReIndexContentReq> reIndexContentLinksForm = Form.form(ReIndexContentReq.class)
                .bindFromRequest();
        if (reIndexContentLinksForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(reIndexContentLinksForm))).toObjectNode());
        }

        ReIndexContentReq reIndexContentLinkReq = reIndexContentLinksForm.get();
        boolean reIndexContentLinksRes = false;
        try {
            reIndexContentLinksRes = ContentManager.reIndexContentLink(
                    reIndexContentLinkReq.entityType, reIndexContentLinkReq.linkType,
                    reIndexContentLinkReq.ids);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(reIndexContentLinksRes).toObjectNode());
    }

    public static Result validateResource(){
    Form<GetEntityReq> GetEntityForm = Form.form(GetEntityReq.class)
                .bindFromRequest();
        if (GetEntityForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(GetEntityForm))).toObjectNode());
        }

        GetEntityReq getEntityReq = GetEntityForm.get();
        boolean getEntityRes = false;
        getEntityRes = ContentManager.getEntity(getEntityReq);
        return ok(getResultResponse(getEntityRes).toObjectNode());
    }

    public static Result addRatingAndFeedback() {
        Form<AddEntityInfoReq> GetEntityForm = Form.form(AddEntityInfoReq.class).bindFromRequest();
        if (GetEntityForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(GetEntityForm))).toObjectNode());
        }

        AddEntityInfoReq getEntityReq = GetEntityForm.get();
        GetEntityInfoForAppRes getEntityRes = null;
        try{
            getEntityRes = ContentManager.addRatingAndFeedback(getEntityReq);
        }catch (VedantuException e){
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getEntityRes).toObjectNode());
    }
}
