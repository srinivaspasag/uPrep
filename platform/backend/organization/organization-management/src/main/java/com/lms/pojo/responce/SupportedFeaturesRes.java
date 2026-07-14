package com.lms.pojo.responce;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class SupportedFeaturesRes {
    public SupportedFeaturesRes() {

        super();
        features= new ArrayList<String>();
    }

    public List<String> features;
}
