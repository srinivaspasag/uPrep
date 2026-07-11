package com.vedantu.cmds.pojos.content.tests;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.vedantu.content.enums.EnumBasket.TestType;
import com.vedantu.content.pojos.tests.TestMetadata;
import com.vedantu.content.pojos.tests.TestQuestionSet;

public class OfflineTestData {

	public String id;
	public String name;
	public String code;
	public String desc;
	public List<TestMetadata> testMetadatas;
	public List<String> targetIds;
	public TestType type;
	public long duration;// in minutes
	public long testDate;
	public String orgId;
	public List<OfflineQInfo> qusMarks;
	public List<TestQuestionSet> sets;

	public OfflineTestData(String name, String srcTestCode, String desc,
			List<TestMetadata> testMetadata, List<String> targetId,
			TestType type, long duration, long testDate) {
		super();
		this.name = name;
		this.code = srcTestCode;
		this.desc = desc;
		this.testMetadatas = testMetadata;
		this.targetIds = targetId;
		this.type = type;
		this.duration = duration;
		this.testDate = testDate;
	}

	public void addQuestionSet(TestQuestionSet set) {
		if (sets == null) {
			sets = new ArrayList<TestQuestionSet>();
		}
		sets.add(set);
	}

	public void addQusInfo(OfflineQInfo qInfo) throws Exception {
		if (qusMarks == null) {
			qusMarks = new ArrayList<OfflineQInfo>();
		}
		if (qusMarks.contains(qInfo)) {
			throw new Exception("duplicates question info for question : "
					+ qInfo);
		}
		qusMarks.add(qInfo);
	}

	public TestQuestionSet __getQuestionSet(String setName) {
		if (sets != null && StringUtils.isNotEmpty(setName)) {
			for (TestQuestionSet set : sets) {
				if (StringUtils.equalsIgnoreCase(setName.trim(),
						set.name.trim())) {
					return set;
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{id:").append(id).append(", name:").append(name)
				.append(", code:").append(code).append(", desc:").append(desc)
				.append(", testMetadatas:").append(testMetadatas)
				.append(", targetIds:").append(targetIds).append(", type:")
				.append(type).append(", duration:").append(duration)
				.append(", testDate:").append(testDate).append(", orgId:")
				.append(orgId).append(", qusMarks:").append(qusMarks)
				.append(", sets:").append(sets).append("}");
		return builder.toString();
	}

}
