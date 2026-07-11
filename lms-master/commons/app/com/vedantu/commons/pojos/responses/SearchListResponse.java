package com.vedantu.commons.pojos.responses;

import java.util.HashMap;
import java.util.Map;

public class SearchListResponse<T extends IListResponseObj> extends
		ListResponse<T> {

	public Map<String, Object> facet = new HashMap<String, Object>();
}
