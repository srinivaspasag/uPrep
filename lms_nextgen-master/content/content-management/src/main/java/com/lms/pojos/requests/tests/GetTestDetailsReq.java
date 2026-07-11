package com.lms.pojos.requests.tests;

import com.lms.pojos.requests.AbstractGetContentReq;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetTestDetailsReq extends AbstractGetContentReq {

	public boolean qTypeDistribution;
	public String testState;
	public String attemptId;
	public boolean needSolution;
}
