package com.vedantu.cmds.enums;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.pojos.content.question.EntireQuestion;
import com.vedantu.cmds.pojos.content.question.HintFormat;
import com.vedantu.cmds.pojos.content.question.SolutionFormat;
import com.vedantu.cmds.pojos.content.question.metadata.QuestionStatesFiniteAutomata;
import com.vedantu.content.enums.QuestionType;

public enum QuestionParts {


	TYPE {
		@Override
		public void accumulate(EntireQuestion q, boolean isStart, String v,
				List<String> uuids, boolean inHTML, boolean imgInBase64) {

			if (StringUtils.isNotEmpty(v.trim())) {
				String typeString = v.trim()
						.replaceAll(EntireQuestion.REGEX_HTML_STRIP, "")
						.substring(v.indexOf(":") + 1).trim();
				q.type  = QuestionType.valueOfKey(typeString);
			}
		}
	},
	TEXT {
		@Override
		public void accumulate(EntireQuestion q, boolean isStart, String v,
				List<String> uuids, boolean inHTML, boolean imgInBase64) {
			if (uuids.size() > 0) {
				q.formattedQuestion.uuidImages.addAll(uuids);
			}
			v = QuestionStatesFiniteAutomata.getHTMLOutputFromLatex(v, q, "text", inHTML, imgInBase64);
			if (inHTML) {
				if (q.formattedQuestion.startIndexOfLatexOrImage == null
						|| q.formattedQuestion.startIndexOfLatexOrImage
								.size() == 0) {
					q.formattedQuestion.startIndexOfLatexOrImage = QuestionStatesFiniteAutomata.getStartingIndexesOfRequiredSubstring(v);
					q.formattedQuestion.endIndexOfLatexOrImage = QuestionStatesFiniteAutomata.getEndIndexesOfRequiredSubstring(v);
				}
				if (isStart) {
					q.formattedQuestion.startIndexOfLatexOrImage =QuestionStatesFiniteAutomata.getStartingIndexesOfRequiredSubstring(v);
					q.formattedQuestion.endIndexOfLatexOrImage = QuestionStatesFiniteAutomata.getEndIndexesOfRequiredSubstring(v);
				}
			}
			q.formattedQuestion.newText += isStart
					&& v.trim()
							.replaceAll(EntireQuestion.REGEX_HTML_STRIP, "")
							.toLowerCase().startsWith("question:") ? v
					.substring(v.indexOf(":") + 1) : StringUtils
					.isEmpty(q.formattedQuestion.newText.trim()) ? v
					.replaceFirst("<br>", "") : v;
		}
	},
	SOLUTION {
		@Override
		public void accumulate(EntireQuestion q, boolean isStart, String v,
				List<String> uuids, boolean inHTML, boolean imgInBase64) {
			if (q.formattedSolutions == null) {
				q.formattedSolutions = new ArrayList<SolutionFormat>();
			}
			boolean newSolution = isStart
					&& v.trim()
							.replaceAll(EntireQuestion.REGEX_HTML_STRIP, "")
							.toLowerCase().startsWith("solution:");
			SolutionFormat sf = q.formattedSolutions.isEmpty()
					|| newSolution ? new SolutionFormat()
					: q.formattedSolutions
							.get(q.formattedSolutions.size() - 1);
			if (uuids.size() > 0) {
				sf.uuidImages.addAll(uuids);
			}
			v = QuestionStatesFiniteAutomata.getHTMLOutputFromLatex(v, q, "text", inHTML, imgInBase64);
			if (inHTML) {
				if (sf.startIndexOfLatexOrImage == null
						|| sf.startIndexOfLatexOrImage.size() == 0) {
					sf.startIndexOfLatexOrImage =  QuestionStatesFiniteAutomata.getStartingIndexesOfRequiredSubstring(v);
					sf.endIndexOfLatexOrImage =  QuestionStatesFiniteAutomata.getEndIndexesOfRequiredSubstring(v);
				}
				if (isStart) {
					sf.startIndexOfLatexOrImage =  QuestionStatesFiniteAutomata.getStartingIndexesOfRequiredSubstring(v);
					sf.endIndexOfLatexOrImage =  QuestionStatesFiniteAutomata.getEndIndexesOfRequiredSubstring(v);
				}
			}
			sf.newText += newSolution ? v.substring(v.indexOf(":") + 1)
					: StringUtils.isEmpty(sf.newText.trim()) ? v
							.replaceFirst("<br>", "") : v;
			if (newSolution) {
				q.formattedSolutions.add(sf);
			}
		}
	},

	OPTIONS {
		@Override
		public void accumulate(EntireQuestion q, boolean isStart, String v,
				List<String> uuids, boolean inHTML, boolean imgInBase64) {
			if (uuids.size() > 0) {
				q.formattedOptions.uuidImages.addAll(uuids);
			}
			// if (!v.trim().toLowerCase().startsWith("options:")) {
			if (v.trim().length() <= 0) {
				return;
			}
			v =  QuestionStatesFiniteAutomata.getHTMLOutputFromLatex(v, q, "options", inHTML, imgInBase64);
			if (inHTML) {
				if (q.formattedOptions.startIndexOfLatexOrImage == null
						|| q.formattedOptions.startIndexOfLatexOrImage
								.size() == 0) {

					LOGGER.info("start index:::"
							+ q.formattedOptions.startIndexOfLatexOrImage);
					LOGGER.info("end index:::"
							+ q.formattedOptions.endIndexOfLatexOrImage);
					q.formattedOptions.startIndexOfLatexOrImage =  QuestionStatesFiniteAutomata.getStartingIndexesOfRequiredSubstring(v);
					q.formattedOptions.endIndexOfLatexOrImage =  QuestionStatesFiniteAutomata.getEndIndexesOfRequiredSubstring(v);
				}
			}
			String sOptn = v.trim()
					.replaceAll(EntireQuestion.REGEX_HTML_STRIP, "")
					.toLowerCase().startsWith("options:") ? StringUtils
					.substringAfter(v, ":") : v;
			if (isStart && StringUtils.isNotEmpty(sOptn)) {
				sOptn = sOptn.replace("\n", "");
				q.formattedOptions.newOptions.add(sOptn);
			} else if (StringUtils.isNotEmpty(sOptn)) {

				int i = q.formattedOptions.newOptions.size() - 1;
				if (i >= 0) {
					String o = q.formattedOptions.newOptions.get(i) + sOptn;
					q.formattedOptions.newOptions.set(i, o);
				} else if (i == -1 && !isStart) {
					sOptn = sOptn.replace("\n", "");
					q.formattedOptions.newOptions.add(sOptn);
				}
			}
		}
		// }
	},
	ANSWER {
		@Override
		public void accumulate(EntireQuestion q, boolean isStart, String v,
				List<String> uuids, boolean inHTML, boolean imgInBase64) {
			if (v.trim().toLowerCase()
					.replaceAll(EntireQuestion.REGEX_HTML_STRIP, "")
					.startsWith("answer:")) {
				v = v.substring(v.indexOf(":") + 1);
			}
			if (v.length() <= 0) {
				return;
			}

			if (q.type != null
					&& q.type.toString().toLowerCase().equals("grid")) {
				String s = v.trim();
				if (s.length() <= 0) {
					return;
				}
				v = v.substring(v.indexOf(":") + 1);
				if (v.length() <= 0) {
					return;
				}
				String[] ans = v.trim().split("\\s");
				String key = ans[0];
				Set<String> corrAns = new HashSet<String>();
				for (int i = 1; i < ans.length; i++) {
					corrAns.add(ans[i].replaceAll(
							EntireQuestion.REGEX_HTML_STRIP, "").trim());
				}
				q.gridAnswer.put(
						key.replaceAll(EntireQuestion.REGEX_HTML_STRIP, "")
								.trim(), corrAns);
			} else {

				v =  QuestionStatesFiniteAutomata.getHTMLOutputFromLatex(v, q, "options", inHTML,
						imgInBase64);
				LOGGER.debug("Answer accumulation" + v + q.answer);
				QuestionType qType = q.type;
				if (qType == QuestionType.MCQ || qType == QuestionType.PARA || qType == QuestionType.MATRIX) {
					q.answer = q.answer + v + ",";
				} else {
					q.answer = q.answer + v;
				}
			}
		}
	},

	COLUMNA {
		@Override
		public void accumulate(EntireQuestion q, boolean isStart, String v,
				List<String> uuids, boolean inHTML, boolean imgInBase64) {
			if (v.replaceAll(EntireQuestion.REGEX_HTML_STRIP, "")
					.toLowerCase().startsWith("columna:")) {
				v = v.substring(v.indexOf(":") + 1);
			}
			if (v.length() <= 0) {
				return;
			}
			q.cola.add(v);
		}
	},
	COLUMNB {
		@Override
		public void accumulate(EntireQuestion q, boolean isStart, String v,
				List<String> uuids, boolean inHTML, boolean imgInBase64) {
			if (v.replaceAll(EntireQuestion.REGEX_HTML_STRIP, "")
					.toLowerCase().startsWith("columnb:")) {
				v = v.substring(v.indexOf(":") + 1);
			}
			if (v.length() <= 0) {
				return;
			}
			q.colb.add(v);
		}
	},
	HINT {
		@Override
		public void accumulate(EntireQuestion q, boolean isStart, String v,
				List<String> uuids, boolean inHTML, boolean imgInBase64) {
			if (q.formattedHints == null) {
				q.formattedHints = new ArrayList<HintFormat>();
			}
			boolean newHint = isStart
					&& v.trim()
							.replaceAll(EntireQuestion.REGEX_HTML_STRIP, "")
							.toLowerCase().startsWith("hints:");
			HintFormat hf = q.formattedHints.isEmpty() || newHint ? new HintFormat()
					: q.formattedHints.get(q.formattedHints.size() - 1);
			if (uuids.size() > 0) {
				hf.uuidImages.addAll(uuids);
			}
			v =  QuestionStatesFiniteAutomata.getHTMLOutputFromLatex(v, q, "text", inHTML, imgInBase64);
			if (inHTML) {
				if (hf.startIndexOfLatexOrImage == null
						|| hf.startIndexOfLatexOrImage.size() == 0) {
					hf.startIndexOfLatexOrImage =  QuestionStatesFiniteAutomata.getStartingIndexesOfRequiredSubstring(v);
					hf.endIndexOfLatexOrImage =  QuestionStatesFiniteAutomata.getEndIndexesOfRequiredSubstring(v);
				}
				if (isStart) {
					hf.startIndexOfLatexOrImage =  QuestionStatesFiniteAutomata.getStartingIndexesOfRequiredSubstring(v);
					hf.endIndexOfLatexOrImage =  QuestionStatesFiniteAutomata.getEndIndexesOfRequiredSubstring(v);
				}
			}
			hf.newText += newHint ? v.substring(v.indexOf(":") + 1)
					: StringUtils.isEmpty(hf.newText.trim()) ? v
							.replaceFirst("<br>", "") : v;
			if (newHint) {
				q.formattedHints.add(hf);
				LOGGER.info("hint: " + hf.newText);
			}
		}
	};

	public  void accumulate(EntireQuestion questionType,
			boolean isStart, String v, List<String> uuids, boolean inHTML,
			boolean imgInBase64){
		return ;
	}
	private static final ALogger LOGGER = Logger.of(QuestionParts.class);

}
