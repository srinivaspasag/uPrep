package com.vedantu.board.pojos.requests;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;
import play.mvc.Http.MultipartFormData;

import com.vedantu.commons.enums.boards.GradeType;

public class UploadConsumerBoardReq extends UploadGlobalBoardReq {

	private static final ALogger LOGGER = Logger
			.of(UploadConsumerBoardReq.class);

	public String treeName;
	public Set<GradeType> grades;

	public UploadConsumerBoardReq(MultipartFormData body) {
		super(body);

		Map<String, String[]> form = body.asFormUrlEncoded();
		for (Map.Entry<String, String[]> entry : form.entrySet()) {
			LOGGER.debug(entry.getKey() + " --> "
					+ StringUtils.join(entry.getValue(), ", "));
		}

		treeName = _getValueFromMultipart(form, "treeName");

		String[] gradesArr = _getValuesFromMultipart(form, "grades");
		LOGGER.debug("gradesArr: " + StringUtils.join(gradesArr, ", "));
		if (null == gradesArr) {
			String gradeVal = _getValueFromMultipart(form, "grades");
			if (StringUtils.isNotEmpty(gradeVal)) {
				gradesArr = new String[] { gradeVal };
			} else {
				return;
			}
		}
		for (String grade : gradesArr) {
			if (StringUtils.isEmpty(grade)) {
				continue;
			}
			GradeType gradeType = GradeType.valueOfKey(grade);
			if (GradeType.UNKNOWN != gradeType) {
				if (null == grades) {
					grades = new HashSet<GradeType>();
				}
				grades.add(gradeType);
			}
		}
	}

	public String validate() {
		String superValidate = super.validate();
		if (null != superValidate) {
			return superValidate;
		}
		if (null == treeName) {
			return "treeName missing";
		}
		if (null == grades) {
			return "grades missing";
		}
		return null;
	}

}
