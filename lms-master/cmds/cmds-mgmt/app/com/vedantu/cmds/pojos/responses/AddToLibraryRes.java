package com.vedantu.cmds.pojos.responses;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.responses.ListResponse;

public class AddToLibraryRes extends ListResponse<EntityResponse> {

    @Required
    public String userId;

    public AddToLibraryRes(String userId) {

        super();
        this.userId = userId;
    }

}
