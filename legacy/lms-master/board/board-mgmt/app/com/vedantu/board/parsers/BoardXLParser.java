package com.vedantu.board.parsers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.board.enums.BoardContextType;
import com.vedantu.board.pojos.BoardNode;
import com.vedantu.board.utils.XLParserUtils;
import com.vedantu.commons.utils.VedantuStringUtils;

public class BoardXLParser {

	private static final ALogger LOGGER = Logger.of(BoardXLParser.class);

	private Set<String> seenBoardNames = new HashSet<String>();
	private Set<BoardNode> rootNodes = new HashSet<BoardNode>();
	private List<BoardNode> prevRecordNodes = new ArrayList<BoardNode>();

	private String fileName;
	private File file;
	private BoardContextType context;
	private int maxAllowedColumns = -1;
	private Workbook wb;
	private Sheet sheet;
	private Iterator<Row> rowIterator;
	private int rowNum;
	private List<String> errors;
	private Set<String> cNames = new HashSet<String>();
	private Set<String> sameAndSimilarCNames = new HashSet<String>();
	private Set<String> parentContextBoardCNames = new HashSet<String>();
	private Map<String, BoardNode> cNamewiseBoardNodes = new HashMap<String, BoardNode>();
	private Set<BoardNode> allBoardNodes = new HashSet<BoardNode>();

	public BoardXLParser(String fileName, File file, BoardContextType context) {
		this.fileName = fileName;
		this.file = file;
		this.context = context;
		LOGGER.debug("parsing fileName: " + fileName + " stored at path: "
				+ file.getAbsolutePath() + " in context: " + context);
		if (BoardContextType.ORG == context
				|| BoardContextType.CONSUMER == context) {
			maxAllowedColumns = 3;
		}

		try {
			wb = WorkbookFactory.create(file);
			sheet = wb.getSheetAt(0);
			rowIterator = sheet.rowIterator();

			parse();

			removeNonRootNodes();

		} catch (Exception e) {
			LOGGER.error("could not parse board file: " + fileName
					+ ", error: " + e.getMessage(), e);
		}
	}

	private void parse() throws Exception {
		while (rowIterator.hasNext()) {
			rowNum++;
			Row row = rowIterator.next();

			StringBuilder sb = new StringBuilder("[ROW-").append(rowNum)
					.append("] ");
			int cellNum = 0;
			boolean isFirst = true;
			boolean foundSomeError = false;

			List<BoardNode> currRecordNodes = new ArrayList<BoardNode>(
					prevRecordNodes);

			int prevColIndex = -1;
			for (Iterator<Cell> cit = row.cellIterator(); cit.hasNext();) {
				cellNum++;
				Cell cell = cit.next();
				int colIndex = cell.getColumnIndex();
				int colNum = colIndex + 1;

				String cellValue = XLParserUtils.getCellValueAsString(cell);
				if (StringUtils.isEmpty(cellValue)) {
					continue;
				}
				if (maxAllowedColumns > 0 && colNum > maxAllowedColumns) {
					accumulateError(rowNum, colNum,
							"found extra values on row " + rowNum + " column "
									+ colNum + " [maxAllowedColumns: "
									+ maxAllowedColumns + " for context: "
									+ context + "]");
					foundSomeError = true;
				}
				if (prevColIndex != -1 && colIndex - prevColIndex > 1) {
					accumulateError(rowNum, colNum,
							"found missing values on row " + rowNum
									+ " between columns " + (prevColIndex + 1)
									+ " and " + colNum);
					foundSomeError = true;
				}

				BoardNode boardNode = new BoardNode(rowNum, colNum, cellValue,
						context);
				if (!boardNode.hasName()) {
					accumulateError(rowNum, colNum,
							"no name found for board on row " + rowNum
									+ " column " + colNum);
					foundSomeError = true;
				} else {
					String cName = boardNode.getCanonicalName();
//					if (BoardContextType.GLOBAL != context
//							&& seenBoardNames.contains(cName)) {
//						accumulateError(rowNum, colNum,
//								"found duplicate board (name: "
//										+ boardNode.name + ") on row " + rowNum
//										+ " column " + colNum + ", cName: "
//										+ cName);
//						foundSomeError = true;
//					} else {
//						seenBoardNames.add(cName);
//					}
					seenBoardNames.add(cName);
				}
				LOGGER.debug("colIndex: " + colIndex
						+ ", currRecordNodes.size:" + currRecordNodes.size()
						+ ", cellValue: " + cellValue);
				if (colIndex < currRecordNodes.size()) {

					currRecordNodes.set(colIndex, boardNode);
					collectInfo(boardNode);

					List<BoardNode> toRemove = new ArrayList<BoardNode>();
					for (int i = colIndex + 1; i < currRecordNodes.size(); i++) {
						toRemove.add(currRecordNodes.get(i));
					}
					LOGGER.debug("toRemove.size: " + toRemove.size());
					boolean areRemoved = currRecordNodes.removeAll(toRemove);
					LOGGER.debug("after removal currRecordNodes.size: "
							+ currRecordNodes.size() + ", areRemoved: "
							+ areRemoved);
				} else if (currRecordNodes.size() == colIndex) {

					currRecordNodes.add(boardNode);
					collectInfo(boardNode);

					LOGGER.debug("added board currRecordNodes.size: "
							+ currRecordNodes.size());
				} else {
					accumulateError(rowNum, colNum, "ignoring cell on row "
							+ rowNum + " column " + colNum);
					foundSomeError = true;
					LOGGER.error("ignoring board at rowNum " + rowNum
							+ " colNum " + colNum);
				}

				sb.append(!isFirst ? ", " : "")
						.append("[COL-" + cellNum + "-" + colIndex + "]:")
						.append(cellValue);
				isFirst = false;
				prevColIndex = colIndex;

			}
			LOGGER.debug(sb.toString());

			if (foundSomeError) {
				LOGGER.error("found some error while parsing " + fileName
						+ " stored at " + file.getAbsolutePath());
				return;
			}

			BoardNode parentNode = null;
			for (BoardNode node : currRecordNodes) {
				if (null == parentNode) {
					rootNodes.add(node);
				} else {
					node.setParent(parentNode);
					parentNode.addChild(node);
				}
				parentNode = node;
			}

			prevRecordNodes = currRecordNodes;

		}
	}

	private void accumulateError(int rowNum, int colNum, String errorMsg) {
		if (null == errors) {
			errors = new ArrayList<String>();
		}
		errors.add("[row=" + rowNum + ", col=" + colNum + "]:" + errorMsg);
	}

	private void removeNonRootNodes() {
		LOGGER.debug("rootNodes.size: " + rootNodes.size());
		Set<BoardNode> toRemove = new HashSet<BoardNode>();
		for (BoardNode rootNode : rootNodes) {
			if (null != rootNode.parent) {
				LOGGER.debug("removing rootNode : " + rootNode
						+ " as it has a parent : " + rootNode.parent);
				toRemove.add(rootNode);
			}
		}
		boolean areRemoved = rootNodes.removeAll(toRemove);
		LOGGER.debug("toRemove.size: " + toRemove.size() + ", areRemoved: "
				+ areRemoved + ", rootNodes.size: " + rootNodes.size());
	}

	public boolean hasErrors() {
		return CollectionUtils.isNotEmpty(errors);
	}

	public List<String> getErrors() {
		return errors;
	}

	public Set<BoardNode> getRootNodes() {
		return rootNodes;
	}

	public Set<String> getCNames() {
		return cNames;
	}

	public Set<String> getSameAndSimilarCNames() {
		return sameAndSimilarCNames;
	}

	public Set<BoardNode> getAllBoardNodes() {
		return allBoardNodes;
	}

	public Set<String> getUnseenCNames() {
		Set<String> unseenCNames = new HashSet<String>(sameAndSimilarCNames);
		boolean removed = unseenCNames.removeAll(cNames);
		LOGGER.debug("cNames.size: " + cNames.size()
				+ ", sameAndSimilarCNames.size: " + sameAndSimilarCNames.size()
				+ ", removed: " + removed + ", unseenCNames.size: "
				+ unseenCNames.size());
		return unseenCNames;
	}

	public Set<String> getParentContextBoardCNames() {
		return parentContextBoardCNames;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (BoardNode rootNode : rootNodes) {
			sb.append(rootNode.toTreeAsString(0, "|     ", "|--   "));
		}
		return sb.toString();
	}

	private void collectInfo(BoardNode boardNode) {
		if (null == boardNode) {
			return;
		}

		allBoardNodes.add(boardNode);

		cNamewiseBoardNodes.put(boardNode.getCanonicalName(), boardNode);

		cNames.add(boardNode.getCanonicalName());
		cNames.addAll(VedantuStringUtils.toCanonical(new ArrayList<String>(
				boardNode.addAliases)));

		sameAndSimilarCNames
				.addAll(VedantuStringUtils.toCanonical(new ArrayList<String>(
						boardNode.sameAsBoardNames)));
		sameAndSimilarCNames.addAll(VedantuStringUtils
				.toCanonical(new ArrayList<String>(
						boardNode.similarToBoardNames)));

		if (StringUtils.isNotEmpty(boardNode.parentContextBoardName)) {
			parentContextBoardCNames.add(VedantuStringUtils
					.toCanonicalName(boardNode.parentContextBoardName));
		}
	}
}
