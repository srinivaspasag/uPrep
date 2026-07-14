package com.lms.pojos.requests;


import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class EditChannelReq extends AddChannelReq {
    @NotBlank(message = "id should not be empty")
    public String id;

    @Override
    public String toString() {
        return " [id=" + id + ", toString()=" + super.toString() + "]";
    }
}
