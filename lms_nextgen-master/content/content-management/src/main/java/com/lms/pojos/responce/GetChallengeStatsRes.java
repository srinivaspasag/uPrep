package com.lms.pojos.responce;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class GetChallengeStatsRes {
    public Map<String, Object> facet;
}
