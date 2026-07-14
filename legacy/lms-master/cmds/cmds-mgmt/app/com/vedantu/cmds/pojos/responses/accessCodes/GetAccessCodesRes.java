package com.vedantu.cmds.pojos.responses.accessCodes;

import com.vedantu.cmds.pojos.AccessCodeInfo;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.commons.pojos.responses.ListResponse;

public class GetAccessCodesRes implements IListResponseObj{
	public ListResponse<AccessCodeInfo> accessCodes;
}
