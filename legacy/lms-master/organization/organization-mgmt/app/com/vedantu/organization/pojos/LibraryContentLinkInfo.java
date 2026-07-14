package com.vedantu.organization.pojos;

import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.mongo.VedantuRecordState;
public class LibraryContentLinkInfo extends ModelBasicInfo implements IListResponseObj {
	
	public ModelBasicInfo source;
	public ModelBasicInfo target;
	public Scope scope;

	
	public LibraryContentLinkInfo(String id, VedantuRecordState recordState) {
		super(id, recordState);
		// TODO Auto-generated constructor stub
	}
	
	
}