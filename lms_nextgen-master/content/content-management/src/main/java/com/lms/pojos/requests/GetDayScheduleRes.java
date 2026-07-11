package com.lms.pojos.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GetDayScheduleRes {
    public List<SubMetadata> metadata = new ArrayList<SubMetadata>();
}
