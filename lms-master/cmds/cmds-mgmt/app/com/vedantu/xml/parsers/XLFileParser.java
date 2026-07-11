package com.vedantu.xml.parsers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.vedantu.board.utils.XLParserUtils;
import com.vedantu.cmds.daos.TempParsedDataDAO;
import com.vedantu.cmds.models.TempParsedDATA;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.VedantuStringUtils;
import com.vedantu.content.daos.TestDAO;
import com.vedantu.content.models.tests.Test;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.models.OrgMember;

import play.Logger;
import play.Logger.ALogger;

public class XLFileParser {

	private static final ALogger LOGGER = Logger.of(XLFileParser.class);
	private File xlFile;
	final private List<String> sheetNames = new ArrayList<String>();

	public XLFileParser(File xlFile) {
		this.xlFile = xlFile;
	}

	/**
	 *
	 * @param handaler
	 * @throws IOException
	 * @throws OpenXML4JException
	 * @throws SAXException
	 * @return unique UUID for this parse session
	 *         AbstractSheetHandaler(handaler), the uuid can be accessed through
	 *         handaler.getUuid
	 */
	public void parse(AbstractSheetHandaler handaler) throws IOException,
			OpenXML4JException, SAXException {
		OPCPackage pkg = OPCPackage.open(xlFile);
		XSSFReader r = new XSSFReader(pkg);
		SharedStringsTable sst = r.getSharedStringsTable();

		InputStream workbookData = r.getWorkbookData();

		InputSource workSource = new InputSource(workbookData);
		XMLReader workBookParser = fetchWorkbookParser(sheetNames);
		workBookParser.parse(workSource);
		IOUtils.closeQuietly(workbookData);
		// System.out.println("sheetNames: " + sheetNames);
		XMLReader parser = fetchSheetParser(sst, handaler);
		// rId2 found by processing the Workbook
		// Seems to either be rId# or rSheet#
		Iterator<InputStream> sheets = r.getSheetsData();
		int i = 1;

		while (sheets.hasNext()) {
			LOGGER.info("sheet index: " + i);
			handaler.setSheetId(i);
			handaler.setSheetName(sheetNames.get(i - 1));
			InputStream sheet = sheets.next();
			InputSource sheetSource = new InputSource(sheet);
			parser.parse(sheetSource);
			IOUtils.closeQuietly(sheet);
			i++;
		}
	}


    public void parse2(AbstractSheetHandaler handaler, String testId, String orgId, SrcEntity target) throws IOException, OpenXML4JException,
            SAXException, VedantuException {
        Workbook wb = WorkbookFactory.create(xlFile);
        int numberOfSheets = wb.getNumberOfSheets();
        LOGGER.debug("Total number of Sheets : "+numberOfSheets);
        if(numberOfSheets == 1){
            Test test = TestDAO.INSTANCE.getById(testId);
            int quesCount = test.qusCount;
            int quesCounter = 0;
            Sheet sheet = wb.getSheetAt(0);
            LOGGER.debug("But we are concerned about only first sheet");
            int rowNumber = sheet.getFirstRowNum();

            boolean hasNonEmptyValue = false;
            boolean hasTestCode = false;
            boolean hasMemberId = false;
            Map<Integer,Integer> questionsCells = new HashMap<Integer, Integer>();
            while(true){
                LOGGER.debug("Iterating over row "+rowNumber);
                if(rowNumber > sheet.getLastRowNum()){
                    break;
                }
                int userQuesCounter = 1;
                TempParsedDATA tempParseddata = new TempParsedDATA();
                if(rowNumber > 1 && quesCount != quesCounter){
                    throw new VedantuException(VedantuErrorCode.INVALID_INPUT_DATA, "Questions count does not match");
                }
                if(rowNumber > 1 && !hasTestCode && !hasMemberId){
                    throw new VedantuException(VedantuErrorCode.INVALID_INPUT_DATA, "Testcode/MemberId fields are missing");
                }
                //Row row = rowIterator.next();
                Row row = sheet.getRow(rowNumber);
                int cellNumber  = row.getFirstCellNum();
                tempParseddata.uuid = handaler.getUuid();
                tempParseddata.sheetId = 1;
                tempParseddata.sheetName = sheet.getSheetName();
                tempParseddata.rowNo = rowNumber;
                for(int i=row.getFirstCellNum();i<row.getLastCellNum();i++){

                    hasNonEmptyValue  = false;
                    Cell cell = row.getCell(i);
                    String cellValue = StringUtils.EMPTY;
                    if(cell != null){
                        cellValue = XLParserUtils.getCellValueAsString(cell,
                                VedantuStringUtils.YYYY_MM_DD);
                    }
                    if (StringUtils.isNotEmpty(cellValue)) {
                        hasNonEmptyValue = true;
                    }
                    LOGGER.debug("Iterating over coloumn "+cellNumber+" value : "+cellValue);
                    if(rowNumber == 0 && cellNumber == 0){
                        if(hasNonEmptyValue && cellValue.equalsIgnoreCase("TESTCODE")){
                            hasTestCode  = true;
                        }else{
                            throw new VedantuException(VedantuErrorCode.INVALID_INPUT_DATA, "Row 1, Cell 1 should have heading as TESTCODE");
                        }
                    }else if(rowNumber == 0 && cellNumber == 1){
                        if(hasNonEmptyValue && cellValue.equalsIgnoreCase("MEMBERID")){
                            hasMemberId = true;
                        }else{
                            throw new VedantuException(VedantuErrorCode.INVALID_INPUT_DATA, "Row 1, Cell 2 should have heading as MEMBERID");
                        }
                    }else if(rowNumber == 0 && hasNonEmptyValue && cellNumber > 1){
                        if(cellValue.equalsIgnoreCase("Q"+(quesCounter+1))){
                            quesCounter++;
                            questionsCells.put(quesCounter, cellNumber);
                        }else{
                            LOGGER.debug("Coloumn "+cellNumber+" of Row "+rowNumber+" has Unwanted data "+cellValue);
                            throw new VedantuException(VedantuErrorCode.INVALID_INPUT_DATA, "Unwanted field in excel "+cellValue);
                        }
                    }


                    if(rowNumber > 0 && hasNonEmptyValue && cellNumber == 0){
                        if(cellValue.equalsIgnoreCase(test.code)){
                            cellNumber++;
                            continue;
                        }else{
                            throw new VedantuException(VedantuErrorCode.INVALID_INPUT_DATA, "Coloumn 1 should have valid TestCode");
                        }
                    }else if(rowNumber > 0 && hasNonEmptyValue && cellNumber == 1){
                        if(isValidMemberId(cellValue, orgId)){
                            tempParseddata.data.put("memberId", cellValue);
                            cellNumber++;
                            continue;
                        }else{
                            throw new VedantuException(VedantuErrorCode.INVALID_INPUT_DATA, "Coloumn 2 should have valid MemberId");
                        }
                    }else if(rowNumber > 0 && cellNumber > 1){
                        if(cellNumber == questionsCells.get(userQuesCounter)){
                            if(hasNonEmptyValue){
                                tempParseddata.data.put("q"+userQuesCounter, cellValue);
                            }
                            userQuesCounter++;
                        }
                    }
                    cellNumber++;
                }
                if(rowNumber > 0){
                    tempParseddata.data.put("testCode", test.code);
                    tempParseddata.data.put("testId", test._getStringId());
                    tempParseddata.data.put("targetType", target.type.name());
                    tempParseddata.data.put("targetId", target.id);
                    TempParsedDataDAO.INSTANCE.save(tempParseddata);
                }
                rowNumber++;
            }
        }else{
            throw new VedantuException(VedantuErrorCode.INVALID_INPUT_DATA, "Sheet count should be exactly 1");
        }
    }

	private boolean isValidMemberId(String memberId, String orgId) {
	    OrgMember orgMember = OrgMemberDAO.INSTANCE.getMemberByMemberId(orgId, memberId);
	    if(orgMember == null){
	        return false;
	    }
        return true;
    }

    public List<String> getSheetNames() {
		return sheetNames;
	}

	private static XMLReader fetchWorkbookParser(List<String> sheetNames)
			throws SAXException {
		XMLReader parser = new VedantuSAXParser();
		WorkBookDataHandaler handler = new WorkBookDataHandaler(sheetNames);
		parser.setContentHandler(handler);
		return parser;
	}

	private static XMLReader fetchSheetParser(SharedStringsTable sst,
			AbstractSheetHandaler handler) throws SAXException {
		XMLReader parser = new VedantuSAXParser();
		handler.setSst(sst);
		parser.setContentHandler(handler);
		return parser;
	}
}
