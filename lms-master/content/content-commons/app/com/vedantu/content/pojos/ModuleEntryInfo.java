package com.vedantu.content.pojos;

import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.content.models.ModuleEntry;
import com.vedantu.content.search.details.AbstractBoardSearchEntityTagDetails;

public class ModuleEntryInfo extends ModuleEntry {
    public ModelBasicInfo info;
    public boolean completed;    //is Completed by user
    public boolean attempted;    //test or assignment attempted by user.
    public AbstractBoardSearchEntityTagDetails boardDetails;
}