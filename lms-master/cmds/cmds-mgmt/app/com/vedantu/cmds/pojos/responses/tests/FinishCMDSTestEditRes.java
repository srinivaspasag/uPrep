package com.vedantu.cmds.pojos.responses.tests;

public class FinishCMDSTestEditRes {
	public String id;
	public boolean success;

	public FinishCMDSTestEditRes(String id, boolean success) {
		super();
		this.id = id;
		this.success = success;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{id:");
		builder.append(id);
		builder.append(", success:");
		builder.append(success);
		builder.append("}");
		return builder.toString();
	}

}
