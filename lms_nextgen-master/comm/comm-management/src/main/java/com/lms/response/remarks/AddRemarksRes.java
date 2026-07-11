package com.lms.response.remarks;

import com.lms.pojos.RemarkInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddRemarksRes {

    public RemarkInfo info;
    public boolean success;

    public AddRemarksRes() {

        super();
        info = null;
        success = false;
    }
}
