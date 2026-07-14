package com.lms.pojos.responce;

import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class AddToLibraryRes extends ListResponse<EntityResponse> {

    @NotBlank(message = "userId should not be null")
    public String userId;

    public AddToLibraryRes(String userId) {

        super();
        this.userId = userId;
    }

}
