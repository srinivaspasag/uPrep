package com.lms.board.pojos.requests;

import com.lms.board.enums.BoardContextType;
import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.enums.BoardType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class GetChildrenReq extends AbstractAuthCheckReq {

    @NotBlank
    public BoardContextType context;
    public String ownerId = "SYSTEM";
    // Required for non-GLOBAL context
    public BoardType type;
    public String parentId;
    public VedantuRecordState recordState;
    public String showSharedSubjects;

    public String validate() {

        /*String superValidate = super.validate();
        if (!StringUtils.isEmpty(superValidate)) {
            return superValidate;
        }
        if (null == context) {
            return "context missing";
        }
        if (BoardContextType.GLOBAL != context && null == type) {
            return "type missing";
        }*/
        return null;
    }

}
