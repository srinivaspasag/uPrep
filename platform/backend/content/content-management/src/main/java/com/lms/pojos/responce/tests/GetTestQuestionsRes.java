package com.lms.pojos.responce.tests;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.EnumBasket.TestType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class GetTestQuestionsRes extends ModelBasicInfo {

	public String name;
	public long duration;
	public TestType type;
	public String code;
	public boolean enableSectionLocking;
	public boolean enablePartialMarks;
	public List<String> partialMarksQTypes;
	public List<String> oneOrMoreMarksQTypes;
	public long totalTestTime;
	public List<TestBoardWiseQuestions> boards;
	public int totalMarks;

	public GetTestQuestionsRes(String id, VedantuRecordState recordState) {
		super(id, recordState);
	}

	public GetTestQuestionsRes(String id, VedantuRecordState recordState,
							   String name, long duration, TestType type, String code) {
		super(id, recordState);
		this.name = name;
		this.duration = duration;
		this.type = type;
		this.code = code;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{name:");
		builder.append(name);
		builder.append(", duration:");
		builder.append(duration);
		builder.append(", type:");
		builder.append(type);
		builder.append(", code:");
		builder.append(code);
		builder.append("}");
		return builder.toString();
	}

}
