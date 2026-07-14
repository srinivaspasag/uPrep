package com.lms.pojos.requests;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Transient;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.pojo.request.AbstractOrgScopeReq;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class EditContentReq extends AbstractOrgScopeReq {

    @Transient
    public final static String DESCRIPTION = "description";
    public final static String NAME        = "name";
    public final static String BOARD_IDS   = "boardIds";

    
    public SrcEntity           entity;

    public String              name;
    public String              description;
    public Set<String>         boardIds;
    public List<String>        cmdsBoardIds;

    public List<String>        updateList  = new ArrayList<String>();

}
