package com.lms.pojos.responce;

import com.lms.pojos.ScheduleDay;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter
public class GetScheduleRes {
    public long month;
    public List<ScheduleDay> days;
}
