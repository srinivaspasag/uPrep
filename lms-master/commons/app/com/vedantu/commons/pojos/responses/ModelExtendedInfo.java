package com.vedantu.commons.pojos.responses;

import com.vedantu.mongo.VedantuRecordState;

public class ModelExtendedInfo extends ModelBasicInfo {
	

    public String name;
	public long timeCreated;
	public long lastUpdated;

	public ModelExtendedInfo(){
	    super();
	}
	public ModelExtendedInfo(String id, VedantuRecordState recordState,
			String name, long timeCreated, long lastUpdated) {
		super(id, recordState);
		this.name = name;
		this.timeCreated = timeCreated;
		this.lastUpdated = lastUpdated;
	}

	@Override
    public String toString() {

        return "ModelExtendedInfo [name=" + name + ", timeCreated=" + timeCreated
                + ", lastUpdated=" + lastUpdated + ", toString()=" + super.toString() + "]";
    }
}
