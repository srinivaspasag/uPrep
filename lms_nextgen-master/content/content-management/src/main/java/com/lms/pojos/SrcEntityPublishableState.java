package com.lms.pojos;

import com.lms.pojos.requests.SrcEntityToUpdate;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SrcEntityPublishableState extends SrcEntityToUpdate {

    public boolean published;
    public String name;
}
