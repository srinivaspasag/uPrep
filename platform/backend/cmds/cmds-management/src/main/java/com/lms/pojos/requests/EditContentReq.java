package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.pojo.request.AbstractOrgScopeReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Getter
@Setter
public class EditContentReq extends AbstractOrgScopeReq {

    @NotBlank
    public final static String DESCRIPTION = "description";
    public final static String NAME        = "name";
    public final static String BOARD_IDS   = "boardIds";

    @NotBlank
    public SrcEntity entity;

    public String              name;
    public String              description;
    public Set<String> boardIds;
    public List<String> cmdsBoardIds;

    public List<String>        updateList  = new ArrayList<String>();

}
