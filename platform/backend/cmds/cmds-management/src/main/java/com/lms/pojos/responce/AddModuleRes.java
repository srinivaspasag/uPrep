package com.lms.pojos.responce;

import com.lms.models.CMDSModule;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddModuleRes {
    public CMDSModule module;
    public boolean success;
}
