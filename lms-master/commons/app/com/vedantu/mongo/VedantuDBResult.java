package com.vedantu.mongo;

import java.util.ArrayList;
import java.util.List;

import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.pojos.responses.ModelExtendedInfo;

public class VedantuDBResult<T extends VedantuBaseMongoModel> {

	public int totalHits;
	public List<T> results = new ArrayList<T>();

	@Override
	public String toString() {
		return "VedantuDBResult [totalHits=" + totalHits + ", results="
				+ results + "]";
	}

	public <B extends ModelBasicInfo> List<B> toBasicInfos() {
		return VedantuBasicDAO._toBasicInfos(results);
	}

	public <E extends ModelExtendedInfo> List<E> toExtendedInfos() {
		return VedantuBasicDAO._toExtendedInfos(results);
	}

}
