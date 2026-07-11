package com.lms.pojos.responce;

import com.lms.pojos.SrcEntityPublishableState;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class EditContentRes {
    public String id;
    public List<SrcEntityPublishableState> contentLists;
    //public String name;
    public boolean isUpdated;
}
