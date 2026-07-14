package com.vedantu.board.pojos.requests;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.board.daos.BoardDAO;
import com.vedantu.board.enums.BoardContextType;
import com.vedantu.commons.enums.boards.BoardType;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.vedantu.mongo.VedantuRecordState;

public class GetChildrenReq extends AbstractAuthCheckReq {

    @Required
    public BoardContextType   context;
    public String             ownerId = BoardDAO.OWNER_SYSTEM;
    // Required for non-GLOBAL context
    public BoardType          type;
    public String             parentId;
    public VedantuRecordState recordState;
    public String             showSharedSubjects;

    public String validate() {

        String superValidate = super.validate();
        if (StringUtils.isNotEmpty(superValidate)) {
            return superValidate;
        }
        if (null == context) {
            return "context missing";
        }
        if (BoardContextType.GLOBAL != context && null == type) {
            return "type missing";
        }
        return null;
    }

}
