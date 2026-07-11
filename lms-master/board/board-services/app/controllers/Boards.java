package controllers;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import play.data.Form;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;

import com.vedantu.board.daos.BoardDAO;
import com.vedantu.board.enums.BoardContextType;
import com.vedantu.board.managers.BoardManager;
import com.vedantu.board.pojos.requests.DeleteBoardReq;
import com.vedantu.board.pojos.requests.GetChildrenReq;
import com.vedantu.board.pojos.requests.GetTargetsReq;
import com.vedantu.board.pojos.requests.GetTreesOfBoardsReq;
import com.vedantu.board.pojos.requests.UploadConsumerBoardReq;
import com.vedantu.board.pojos.requests.UploadGlobalBoardReq;
import com.vedantu.board.pojos.requests.UploadOrgBoardReq;
import com.vedantu.board.pojos.responses.GetChildrenRes;
import com.vedantu.board.pojos.responses.GetTargetsRes;
import com.vedantu.board.pojos.responses.GetTreesRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.boards.GradeType;
import com.vedantu.commons.pojos.responses.ActionTakenRes;

public class Boards extends AbstractVedantuController {

    private static Result validateUploadBoardReq(UploadGlobalBoardReq uploadBoardReq) {

        Result result = null;
        if (null == uploadBoardReq) {
            result = ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        } else {
            String validation = uploadBoardReq.validate();
            if (StringUtils.isNotEmpty(validation)) {
                result = ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, validation))
                        .toObjectNode());
            } else if (null == uploadBoardReq.inputFile) {
                result = ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_FILE))
                        .toObjectNode());
            }
        }
        return result;
    }

    public static Result uploadGlobalBoards() {

        UploadGlobalBoardReq uploadBoardReq = getUploadGlobalBoardReq();
        Result result = validateUploadBoardReq(uploadBoardReq);
        if (null == result) {
            result = uploadBoards(BoardDAO.OWNER_SYSTEM, uploadBoardReq, BoardContextType.GLOBAL,
                    BoardDAO.TREE_SYSTEM, null);
        }
        deleteFile(uploadBoardReq.fileName, uploadBoardReq.inputFile);
        return result;
    }

    public static Result uploadConsumerBoards() {

        UploadConsumerBoardReq uploadBoardReq = getUploadConsumerBoardReq();
        Result result = validateUploadBoardReq(uploadBoardReq);
        if (null == result) {
            result = uploadBoards(BoardDAO.OWNER_SYSTEM, uploadBoardReq, BoardContextType.CONSUMER,
                    uploadBoardReq.treeName, uploadBoardReq.grades);
        }
        deleteFile(uploadBoardReq.fileName, uploadBoardReq.inputFile);
        return result;
    }

    public static Result uploadOrgBoards() {

        UploadOrgBoardReq uploadBoardReq = getUploadOrgBoardReq();
        Result result = validateUploadBoardReq(uploadBoardReq);
        if (null == result) {
            result = uploadBoards(uploadBoardReq.orgId, uploadBoardReq, BoardContextType.ORG,
                    uploadBoardReq.treeName, uploadBoardReq.grades);
        }
        deleteFile(uploadBoardReq.fileName, uploadBoardReq.inputFile);
        return result;
    }

    private static Result uploadBoards(String ownerId, UploadGlobalBoardReq uploadBoardReq,
            BoardContextType contextType, String treeName, Set<GradeType> grades) {

        GetTreesRes getTreesRes;
        try {
            getTreesRes = BoardManager.uploadBoards(ownerId, uploadBoardReq, contextType, treeName,
                    grades);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getTreesRes).toObjectNode());
    }

    private static UploadGlobalBoardReq getUploadGlobalBoardReq() {

        MultipartFormData body = request().body().asMultipartFormData();
        UploadGlobalBoardReq uploadGlobalBoardReq = new UploadGlobalBoardReq(body);
        return uploadGlobalBoardReq;
    }

    private static UploadConsumerBoardReq getUploadConsumerBoardReq() {

        MultipartFormData body = request().body().asMultipartFormData();
        UploadConsumerBoardReq uploadConsumerBoardReq = new UploadConsumerBoardReq(body);
        return uploadConsumerBoardReq;
    }

    private static UploadOrgBoardReq getUploadOrgBoardReq() {

        MultipartFormData body = request().body().asMultipartFormData();
        UploadOrgBoardReq uploadOrgBoardReq = new UploadOrgBoardReq(body);
        return uploadOrgBoardReq;
    }

    public static Result addBoards() {

        return TODO;
    }

    public static Result getChildren() {

        Form<GetChildrenReq> getChildrenForm = Form.form(GetChildrenReq.class).bindFromRequest();
        if (getChildrenForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getChildrenForm))).toObjectNode());
        }
        GetChildrenReq getChildrenReq = getChildrenForm.get();
        GetChildrenRes getChildrenRes = null;
        try {
            getChildrenRes = BoardManager.getChildren(getChildrenReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getChildrenRes).toObjectNode());
    }

    public static Result getTargets() {

        Form<GetTargetsReq> getTargetsForm = Form.form(GetTargetsReq.class).bindFromRequest();
        if (getTargetsForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetTargetsReq getTargetsReq = getTargetsForm.get();
        GetTargetsRes getTargetsRes = null;
        try {
            getTargetsRes = BoardManager.getTargets(getTargetsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getTargetsRes).toObjectNode());
    }

    public static Result getTreesOfBoards() {

        Form<GetTreesOfBoardsReq> getTreesOfBoardsForm = Form.form(GetTreesOfBoardsReq.class)
                .bindFromRequest();
        if (getTreesOfBoardsForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetTreesOfBoardsReq getTreesOfBoardsReq = getTreesOfBoardsForm.get();
        GetTreesRes getTreesRes = null;
        try {
            getTreesRes = BoardManager.getTreesOfBoards(getTreesOfBoardsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getTreesRes).toObjectNode());
    }

    public static Result removeBoardFromTree() {

        Form<DeleteBoardReq> requestForm = Form.form(DeleteBoardReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        DeleteBoardReq request = requestForm.get();
        ActionTakenRes response = new ActionTakenRes();
        try {
            response.done = BoardManager.delete(request.userId, request.brdId,
                    request.parentBrdId);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }
}
