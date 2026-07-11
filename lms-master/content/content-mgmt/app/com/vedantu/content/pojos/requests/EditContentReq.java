package com.vedantu.content.pojos.requests;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import play.data.validation.Constraints.Required;

import com.google.code.morphia.annotations.Transient;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class EditContentReq extends AbstractOrgScopeReq {

    @Transient
    public final static String DESCRIPTION = "description";
    public final static String NAME        = "name";
    public final static String BOARD_IDS   = "boardIds";

    @Required
    public SrcEntity           entity;

    public String              name;
    public String              description;
    public Set<String>         boardIds;
    public List<String>        cmdsBoardIds;

    public List<String>        updateList  = new ArrayList<String>();

}
