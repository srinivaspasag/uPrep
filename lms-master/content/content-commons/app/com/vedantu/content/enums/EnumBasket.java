package com.vedantu.content.enums;

import org.apache.commons.lang3.StringUtils;

public class EnumBasket {
	public static enum Correctness {
		UNKNOWN, CORRECT, INCORRECT, UNJUDGEABLE;

		public static Correctness valueOfKey(String val) {
			Correctness correctness = Correctness.UNKNOWN;
			try {
				correctness = valueOf(StringUtils.defaultIfEmpty(val,
						UNKNOWN.name()).toUpperCase());
			} catch (Exception e) {
			}

			return correctness;
		}
	}

	public static enum Judgement {
		UNKNOWN, JUDGE, DONT_JUDGE, DONT_CARE;

		public static Judgement valueOfKey(String val) {
			Judgement judgement = Judgement.UNKNOWN;
			judgement = valueOf(StringUtils.defaultIfEmpty(val, UNKNOWN.name())
					.toUpperCase());
			// if(judgement==UNKNOWN){
			// throw new Exception("judgement type not recognised!");
			// }

			return judgement;
		}
	}

	public static enum Status {
		COMPLETE, SKIP, REVIEW, UNKNOWN;

		public static Status valueOfKey(String val) {
			Status status = Status.COMPLETE;
			status = valueOf(StringUtils.defaultIfEmpty(val, COMPLETE.name())
					.toUpperCase());
			return status;
		}
	}

	public static enum TestType {
		UNKNOWN, TEST, ASSIGNMENT, QUESTION_LIST, CHALLENGE, ONLINE,OFFLINE;

		public static TestType valueOfKey(String type) {
			TestType testType = UNKNOWN;

			testType = valueOf(StringUtils.defaultIfEmpty(type, TEST.name())
					.toUpperCase());
			if (testType == UNKNOWN) {
				// throw new Exception("Type is not recognised");
			}

			return testType;
		}
	}

	public static enum MarksType {
		AVERAGE, LOWEST, HIGHEST;

		public static MarksType valueOfKey(String type) {
			MarksType marksType = AVERAGE;
			try {
				marksType = valueOf(StringUtils.defaultIfEmpty(type,
						AVERAGE.name()));
			} catch (Exception e) {

			}
			return marksType;
		}
	}
}
