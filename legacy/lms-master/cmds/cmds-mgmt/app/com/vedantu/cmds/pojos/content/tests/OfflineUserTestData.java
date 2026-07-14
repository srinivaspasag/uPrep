package com.vedantu.cmds.pojos.content.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class OfflineUserTestData {

	public String userId;
	public String testCode;// testCode {parent testCode}
	// if there are multiple paper in test than map of testId to UserQusInfo
	public Map<String, List<UserQusInfo>> testToQusMarks;

	public OfflineUserTestData(String testCode) {
		super();
		this.testCode = testCode;
	}

	public void addQusInfo(String testCode, String setName, UserQusInfo qInfo) {
		if (testToQusMarks == null) {
			testToQusMarks = new HashMap<String, List<UserQusInfo>>();
		}
		String key = __getMapCode(testCode, setName);
		if (testToQusMarks.get(key) == null) {
			testToQusMarks.put(key, new ArrayList<UserQusInfo>());
		}
		testToQusMarks.get(key).add(qInfo);
	}

	private String __getMapCode(String testCode, String setName) {
		String key = testCode;
		if (StringUtils.isNotEmpty(setName)) {
			key = testCode + "#" + setName;
		}
		return key;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{userId:").append(userId).append(", testCode:")
				.append(testCode).append(", testToQusMarks:")
				.append(testToQusMarks).append("}");
		return builder.toString();
	}

}
