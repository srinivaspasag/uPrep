package com.vedantu.cmds.pojos.responses.tests;

public class UploadOfflineTestResultRes {

	public boolean processed;
	public String jobId;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{processed:").append(processed).append(", jobId:")
				.append(jobId).append("}");
		return builder.toString();
	}

}
