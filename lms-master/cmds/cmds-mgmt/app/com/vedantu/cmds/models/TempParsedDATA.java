package com.vedantu.cmds.models;

import java.util.HashMap;
import java.util.Map;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "tempparsedata", noClassnameStored = true)
@Indexes(@Index("uuid,sheetId"))
public class TempParsedDATA extends VedantuBaseMongoModel {

	public String uuid;
	public int sheetId;
	public int rowNo;
	public String sheetName;
	public Map<String, String> data = new HashMap<String, String>();

	public TempParsedDATA() {
		super();
	}

	public TempParsedDATA(String uuid, int rowNo, int sheetId,
			String sheetName, Map<String, String> data) {
		super();
		this.uuid = uuid;
		this.rowNo = rowNo;
		this.sheetId = sheetId;
		this.sheetName = sheetName;
		this.data = data;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{uuid:").append(uuid).append(", sheetId:")
				.append(sheetId).append(", rowNo:").append(rowNo)
				.append(", sheetName:").append(sheetName).append(", data:")
				.append(data).append(", id:").append(id)
				.append(", timeCreated:").append(timeCreated)
				.append(", lastUpdated:").append(lastUpdated)
				.append(", recordState:").append(recordState).append("}");
		return builder.toString();
	}

}
