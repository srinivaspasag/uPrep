package com.vedantu.content.pojos.requests;

import java.util.List;

import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public abstract class AbstractAddContentReq extends AbstractAuthCheckReq {

	public List<String> tags;
	public Scope scope;
	public SrcEntity contentSrc;

}
