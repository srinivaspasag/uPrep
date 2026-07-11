package com.vedantu.organization.pojos.responses.organizations;

import java.util.ArrayList;
import java.util.List;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.pojos.responses.IListResponseObj;

public class GetLatestActivityResponseList<T extends IListResponseObj> {

	public long             totalHits;
	public String 			studentName;
	public String 			memberId;
    public List<T>          list = new ArrayList<T>();
    public VedantuErrorCode cumulativeErrorCode;

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{totalHits:");
        builder.append(totalHits);
        builder.append(", list:");
        builder.append(list);
        builder.append("}");
        return builder.toString();
    }
}
