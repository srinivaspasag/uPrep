package com.vedantu.cmds.pojos.content.solution.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.vedantu.cmds.commons.util.LatexProcessor;
import com.vedantu.cmds.pojos.content.question.EntireQuestion;
import com.vedantu.cmds.pojos.content.question.OptionFormat;

public class GridSolutionInfo extends SolutionInfo {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	public List<String> cola;
	public List<String> colb;
	public Map<String, List<String>> gridAnswer;

	public List<String> optionOrdera;
	public List<String> optionOrderb;
	public List<String> originalCola;
	public List<String> originalColb;
	public Map<String, Set<String>> originalGridAnswer;

	public GridSolutionInfo() {
		this(new HashMap<String, Set<String>>(), new OptionFormat(),
				new ArrayList<String>(), new ArrayList<String>());
	}

	public GridSolutionInfo(Map<String, Set<String>> ans, OptionFormat op,
			List<String> ca, List<String> cb) {
		super(op);
		optionOrdera = new ArrayList<String>();
		optionOrderb = new ArrayList<String>();
		this.cola = EntireQuestion.formatOptions(ca, optionOrdera);
		this.colb = EntireQuestion.formatOptions(cb, optionOrderb);
		originalCola = ca;
		originalColb = cb;
		originalGridAnswer = ans;
		this.gridAnswer = new LinkedHashMap<String, List<String>>();
		for (Entry<String, Set<String>> entry : ans.entrySet()) {
			Set<String> matches = entry.getValue();
			List<String> nOptn = new ArrayList<String>();
			for (String m : matches) {
				if (optionOrderb.contains(m)) {
					nOptn.add(Integer.toString((optionOrderb.indexOf(m)) + 1));
				}
			}
			gridAnswer.put(Integer.toString((optionOrdera.indexOf(entry
					.getKey().trim()) + 1)), nOptn);
		}

	}

	@Override
	public void addHook() {
		super.addHook();
		if (cola != null && !cola.isEmpty()) {
			List<String> newCola = new ArrayList<String>();
			for (String option : cola) {
				newCola.add(LatexProcessor.addHookToLatex(option));
			}
			cola = newCola;
		}
		if (colb != null && !colb.isEmpty()) {
			List<String> newColb = new ArrayList<String>();
			for (String option : colb) {
				newColb.add(LatexProcessor.addHookToLatex(option));
			}
			colb = newColb;
		}
	}

}
