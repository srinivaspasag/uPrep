package com.lms.pojos;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter
public class ScheduleDay {
    public long date;
    public List<DayMetadata> metadata;
}
