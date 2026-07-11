package com.lms.board.pojos.requests;

import com.lms.board.enums.BoardContextType;
import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
public class GetTreesOfBoardsReq  extends AbstractAuthCheckReq {
    @NotBlank
    public BoardContextType context;
    public String ownerId = "SYSTEM";
    @NotBlank
    public List<String> treeRootIds;
    public int depth = 3;

    public String validate() {
        String superValidate = super.validate();
        if (!StringUtils.isEmpty(superValidate)) {
            return superValidate;
        }
        if (null == context) {
            return "context missing";
        }
        if (CollectionUtils.isEmpty(treeRootIds)) {
            return "treeRootIds missing";
        }
        return null;
    }
}
