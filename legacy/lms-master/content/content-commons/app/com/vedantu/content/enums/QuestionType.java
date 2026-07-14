package com.vedantu.content.enums;

import com.vedantu.content.enums.EnumBasket.Judgement;
import com.vedantu.content.enums.EnumBasket.Status;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.Logger.ALogger;

import java.util.*;
import java.util.Map.Entry;

public enum QuestionType {
	MCQ {
		@Override
		public boolean isJudgeable() {
			return true;
		}

		@Override
		public AnswerCorrectness isCorrect(Judgement judgement, List<String> answerGiven,
				List<String> correctAnswer, Status status, boolean isPartialMarksAllowed, boolean isOneOrMoreAllowed) {
		    if(!isPartialMarksAllowed) {
		    	LOGGER.info("HEMAN Partial marks no there");
		        return getIsCorrect(judgement, answerGiven, correctAnswer, status, isOneOrMoreAllowed);
            }
            LOGGER.info("HEMAN Partial marks IS there");
			return getIsCorrectForPartial(judgement, answerGiven, correctAnswer, status, isOneOrMoreAllowed);
		}
	},
	SCQ {
		@Override
		public boolean isJudgeable() {
			return true;
		}

		@Override
		public AnswerCorrectness isCorrect(Judgement judgement, List<String> answerGiven,
				List<String> correctAnswer, Status status, boolean isPartialMarksAllowed, boolean isOneOrMoreAllowed) {
			return getIsCorrect(judgement, answerGiven, correctAnswer, status, false);
		}
	},
	MATRIX {
		@Override
		public boolean isJudgeable() {
			return true;
		}

		@Override
		public AnswerCorrectness isCorrect(Judgement judgement, List<String> answerGiven,
				List<String> correctAnswer, Status status, boolean isPartialMarksAllowed, boolean isOneOrMoreAllowed) {
		    if(!isPartialMarksAllowed) {
                LOGGER.info("HEMAN Partial marks no there");
                return getIsCorrect(judgement, answerGiven, correctAnswer, status, isOneOrMoreAllowed);
            }
            LOGGER.info("HEMAN Partial marks IS there");
            return getIsCorrectForPartial(judgement, answerGiven, correctAnswer, status, isOneOrMoreAllowed);
		}
	},
	NUMERIC {
		@Override
		public boolean isJudgeable() {
			return true;
		}

		@Override
		public AnswerCorrectness isCorrect(Judgement judgement, List<String> answerGiven,
				List<String> correctAnswer, Status status, boolean isPartialMarksAllowed, boolean isOneOrMoreAllowed) {
			return getIsNumericallyCorrect(judgement, answerGiven,
					correctAnswer, status);
		}

	},
	TEXT {
		@Override
		public boolean isJudgeable() {
			return false;
		}

		@Override
		public AnswerCorrectness isCorrect(Judgement judgement, List<String> answerGiven,
				List<String> correctAnswer, Status status, boolean isPartialMarksAllowed, boolean isOneOrMoreAllowed) {
			return AnswerCorrectness.INCORRECT;
		}
	},
    PARA {
        @Override
        public boolean isJudgeable() {
            return true;
        }

        @Override
        public AnswerCorrectness isCorrect(Judgement judgement, List<String> answerGiven,
                List<String> correctAnswer, Status status, boolean isPartialMarksAllowed, boolean isOneOrMoreAllowed) {
            if(!isPartialMarksAllowed) {
                LOGGER.info("HEMAN Partial marks no there");
                return getIsCorrect(judgement, answerGiven, correctAnswer, status, isOneOrMoreAllowed);
            }
            LOGGER.info("HEMAN Partial marks IS there");
            return getIsCorrectForPartial(judgement, answerGiven, correctAnswer, status, isOneOrMoreAllowed);
        }
    },
    SUBJECTIVE {
        @Override
        public boolean isJudgeable() {
            return true;
        }

        @Override
        public AnswerCorrectness isCorrect(Judgement judgement, List<String> answerGiven,
                List<String> correctAnswer, Status status, boolean isPartialMarksAllowed, boolean isOneOrMoreAllowed) {
            return AnswerCorrectness.UNKNOWN;
        }
    },
	UNKNOWN {
		@Override
		public boolean isJudgeable() {
			return false;
		}

		@Override
		public AnswerCorrectness isCorrect(Judgement judgement, List<String> answerGiven,
				List<String> correctAnswer, Status status, boolean isPartialMarksAllowed, boolean isOneOrMoreAllowed) {
			return AnswerCorrectness.INCORRECT;
		}
	};

	private static AnswerCorrectness getIsCorrectForPartial(Judgement judgement, List<String> answerGiven,
                                                        List<String> correctAnswer, Status status, boolean isOneOrMoreAllowed) {
	    if(status != Status.COMPLETE) {
	        return AnswerCorrectness.INCORRECT;
        }
	    if(judgement != Judgement.JUDGE) {
	        return AnswerCorrectness.INCORRECT;
        }
	    correctAnswer = getSortedIndex(correctAnswer);
	    if(CollectionUtils.isEqualCollection(answerGiven, correctAnswer)) {
	        return AnswerCorrectness.CORRECT;
        }

        if(CollectionUtils.isEmpty(answerGiven)) {
	        return AnswerCorrectness.INCORRECT;
        }

        Set<String> correctAnswers = new HashSet<String>(correctAnswer);
	    LOGGER.debug("HEMAN Answer given: " + answerGiven);
        LOGGER.debug("HEMAN Correct Answer: " + correctAnswers);
	    for(String answer : answerGiven) {
            if (!correctAnswers.contains(answer)) {
	            return AnswerCorrectness.INCORRECT;
            }
        }
        LOGGER.debug("HEMAN Returning Partial");
		return AnswerCorrectness.PARTIAL;
	}

	private static AnswerCorrectness getIsCorrect(Judgement judgement,
			List<String> answerGiven, List<String> correctAnswer, Status status, boolean isOneOrMoreAllowed) {
		if (status != Status.COMPLETE) {
			return AnswerCorrectness.INCORRECT;
		}
		switch (judgement) {
		case JUDGE:
			return isCorrectlyAnswered(correctAnswer, answerGiven, isOneOrMoreAllowed);
		case DONT_JUDGE:
		case DONT_CARE:
		default:
			return AnswerCorrectness.INCORRECT;
		}
	}

	private static AnswerCorrectness getIsNumericallyCorrect(Judgement judgement,
			List<String> answerGiven, List<String> correctAnswer, Status status) {
		if (status != Status.COMPLETE) {
			return AnswerCorrectness.INCORRECT;
		}
		switch (judgement) {
		case JUDGE:
			return isNumericCorrectlyAnswered(correctAnswer, answerGiven);
		case DONT_JUDGE:
		case DONT_CARE:
		default:
			return AnswerCorrectness.INCORRECT;
		}
	}

	public static QuestionType valueOfKey(String key) {
		QuestionType qType = UNKNOWN;
		try {
			if (StringUtils.equalsIgnoreCase(key.trim().replaceAll("\\s", ""),
					"MatchMatrix")) {
				qType = MATRIX;
			} else {
				qType = valueOf(key.trim().toUpperCase());
			}
		} catch (Exception ignored) {}
		return qType;
	}

	private static final ALogger LOGGER = Logger.of(QuestionType.class);

	public abstract boolean isJudgeable();

	public abstract AnswerCorrectness isCorrect(Judgement judgement,
									 List<String> answerGiven, List<String> correctAnswer, Status status, boolean isPartialMarksAllowed, boolean isOneOrMoreAllowed);

	public static AnswerCorrectness isNumericCorrectlyAnswered(
			List<String> correctAnswer, List<String> answerGiven) {
		if (CollectionUtils.isEmpty(answerGiven)
				|| CollectionUtils.isEmpty(correctAnswer)) {
			LOGGER.error("missing correctAnswer[" + correctAnswer
					+ "] or answerGiven [" + answerGiven + "]");
			return AnswerCorrectness.INCORRECT;
		}
		String answered = answerGiven.get(0).trim();
		String correct = correctAnswer.get(0).trim();
		LOGGER.info("answered: " + answered + " correct: " + correct
				+ " are they both equal? "
				+ StringUtils.equalsIgnoreCase(answered, correct));
		/*
		 * if (StringUtils.equalsIgnoreCase(answered, correct)) {
		 * Logger.log4j.info("exact answer!"); return true; } else if
		 * (answered.length() < correct.length()) { return false; } else if
		 * (answered.length() > correct.length()) { int precision =
		 * correct.trim().length(); Logger.log4j.info("precision: "+precision);
		 * Logger.log4j.info("answer precison: "+answered.substring(0,
		 * precision)); if (StringUtils.equalsIgnoreCase(answered.substring(0,
		 * precision), correct)) { return true; } } return false;
		 */
		try {
			LOGGER.debug("" + (correct.length() - correct.indexOf(".")));
			int correctNumChars = correct.length() - correct.indexOf(".");
			int answeredNumChars = answered.length() - answered.indexOf(".");
			int numChars = 0;
			if (correct.contains(".") && answered.contains(".")) {
				numChars = Math.max(answeredNumChars, correctNumChars) - 1;
				if (answeredNumChars == correctNumChars) {
					numChars = answeredNumChars;
				}
			} else if (!correct.contains(".") && answered.contains(".")) {
				numChars = answeredNumChars - 1;
			} else if (correct.contains(".") && !answered.contains(".")) {
				numChars = correctNumChars - 1;
			} else if (!correct.contains(".") && !answered.contains(".")) {
				numChars = 1;
			}
			LOGGER.debug("num chars: " + numChars);
			double dCorrect = Double.parseDouble(correct);
			double dAnswered = Double.parseDouble(answered);
			LOGGER.debug("dcorrect: " + dCorrect + " dAnswered: " + dAnswered);
			if (dCorrect == dAnswered) {
				return AnswerCorrectness.CORRECT;
			}
			if ((Math.signum(dCorrect) * dCorrect) > (Math.signum(dAnswered) * dAnswered)) {
				return AnswerCorrectness.INCORRECT;
			}
			if(Math.signum(dCorrect) != Math.signum(dAnswered)){
                LOGGER.debug("check sign of variable");
                return AnswerCorrectness.INCORRECT;
            }
			Double tolerance = 1d
					/ Math.pow(10d, Math.max(numChars, 1));
			LOGGER.debug("tolerance: " + tolerance);
			LOGGER.debug("dAnswered-dCorrect: "
					+ (dAnswered - dCorrect - tolerance));
			if (Math.abs(dAnswered) - Math.abs(dCorrect) - tolerance <= 1d / Math.pow(10d, 9)) {
				return AnswerCorrectness.CORRECT;
			}
		} catch (Exception e) {
			LOGGER.error(
					"answer is expected in numeric format - " + e.getMessage(),
					e);
		}
		return AnswerCorrectness.INCORRECT;
	}

	public static AnswerCorrectness isCorrectlyAnswered(List<String> correctAnswer,
			List<String> answerGiven, boolean isOneOrMoreAllowed) {
		LOGGER.info("correct answer: " + correctAnswer);
		correctAnswer = getSortedIndex(correctAnswer);
		LOGGER.info("sorted correct answer: " + correctAnswer);

		if (CollectionUtils.isEqualCollection(correctAnswer, answerGiven)) {
			return AnswerCorrectness.CORRECT;
		}
        if (!isOneOrMoreAllowed) {
            for(String answer : answerGiven){
                if(!correctAnswer.contains(answer)){
                    return AnswerCorrectness.INCORRECT;
                }
            }
            return AnswerCorrectness.CORRECT;
        }
		return AnswerCorrectness.INCORRECT;
	}

	public static List<String> getSortedIndex(List<String> answerGiven) {
		List<Integer> indexes = new ArrayList<Integer>();
		if (answerGiven != null) {
			for (String strInd : answerGiven) {
				try {
					indexes.add(Integer.parseInt(strInd));
					LOGGER.debug("index: " + strInd);
				} catch (NumberFormatException e) {
					Collections.sort(answerGiven);
					return answerGiven;
				}
			}
		}
		answerGiven = new ArrayList<String>();
		for (Integer ind : indexes) {
			answerGiven.add(ind.toString());
		}
		return answerGiven;
	}

	public static AnswerCorrectness isEqualMatrix(
			Map<String, List<String>> correctAnswer,
			Map<String, List<String>> answerGiven) {
		for (Entry<String, List<String>> entry : correctAnswer.entrySet()) {
			List<String> correctAns = entry.getValue();
			List<String> ansGiven = answerGiven.get(entry.getKey());
			boolean equals = CollectionUtils.isEqualCollection(correctAns,
					ansGiven);
			if (!equals) {
				return AnswerCorrectness.INCORRECT;
			}
		}
		return AnswerCorrectness.CORRECT;
	}

}
