package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AbstractListReq extends AbstractAuthCheckReq {
	public String orderBy;
	public String sortOrder;
	public int start;
	public int size;
}
