package com.lms.pojos;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.models.ModuleEntry;
import com.lms.pojos.search.details.AbstractBoardSearchEntityTagDetails;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ModuleEntryInfo extends ModuleEntry {
    public ModelBasicInfo info;
    public boolean completed;    //is Completed by user
    public boolean attempted;    //test or assignment attempted by user.
    public AbstractBoardSearchEntityTagDetails boardDetails;
    public String downloadState;

}