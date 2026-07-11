package com.lms.pojos.responce;

import com.lms.pojos.OrgDetails;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetSharedQuestionsBasicInfoRes {
	public List<OrgDetails> orgDetails;
	public List<OrgDetails> sharedOrgDetails;
}
