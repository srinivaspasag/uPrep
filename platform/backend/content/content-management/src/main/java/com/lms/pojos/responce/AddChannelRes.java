package com.lms.pojos.responce;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddChannelRes extends GetChannelRes
{

    public AddChannelRes(String id, String name) {

        super(id, name, 0);
    }

}
