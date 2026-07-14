package com.lms.pojos.responce;

import com.lms.models.ModuleEntry;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModuleContentAccessStatus {
    public ModuleEntry moduleEntry;
    public boolean accessed;
}

