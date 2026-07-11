package com.lms.parsers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.utils.VedantuStringUtils;
import com.lms.common.utils.XLParserUtils;
import com.lms.common.vedantu.commons.pojos.requests.InputFieldInfo;
import com.lms.pojo.StudentXLRecord;
import com.lms.user.vedantu.user.enums.Gender;
import com.lms.user.vedantu.user.pojo.MemberParentInfo;




public class StudentsXLParser {

	private static final Logger logger = LoggerFactory.getLogger(StudentsXLParser.class);;

    private String                             fileName;
    private File                               file;

    private Workbook                           wb;
    private Sheet                              sheet;
    private Iterator<Row>                      rowIterator;
    private int                                rowNum;
    private List<String>                       errors;
    private Map<String, List<StudentXLRecord>> studentRecords = new LinkedHashMap<String, List<StudentXLRecord>>();
    private Set<String>                        centers        = new HashSet<String>();
    private Set<String>                        sections       = new HashSet<String>();

    private List<InputFieldInfo>               orgExtraInfoFields;
    public static final String CENTER_SECTION_SEPARATOR = "/";
    public StudentsXLParser(String fileName, File file, String progId,
            List<InputFieldInfo> orgExtraInfoFields) {

        this.fileName = fileName;
        this.file = file;
        this.orgExtraInfoFields = orgExtraInfoFields;
        logger.debug("parsing fileName: " + this.fileName + " stored at path: "
                + this.file.getAbsolutePath() + " in progId: " + progId
                + " and orgExtraInfoFields: " + orgExtraInfoFields);

        try {
            wb = WorkbookFactory.create(file);
            sheet = wb.getSheetAt(0);
            rowIterator = sheet.rowIterator();

            parse();

        } catch (Throwable t) {
            logger.error(
                    "could not parse student file: " + fileName + " saved at "
                            + file.getAbsolutePath() + ", error: " + t.getMessage(), t);
        }

    }

    private void parse() throws VedantuException {

        String previousKey = null;
        while (rowIterator.hasNext()) {
            rowNum++;
            Row row = rowIterator.next();

            StringBuilder sb = new StringBuilder("[ROW-").append(rowNum).append("] ");
            int cellNum = 0;
            boolean isFirst = true;
            boolean hasNonEmptyValue = false;

            StudentXLRecord record = new StudentXLRecord(rowNum);

            for (Iterator<Cell> cit = row.cellIterator(); cit.hasNext();) {
                cellNum++;
                Cell cell = cit.next();
                int colIndex = cell.getColumnIndex();
                int colNum = colIndex + 1;

                String cellValue = XLParserUtils.getCellValueAsString(cell,
                        VedantuStringUtils.YYYY_MM_DD);
                if (!StringUtils.isEmpty((cellValue))) {
                    hasNonEmptyValue = true;
                }
                if (rowNum > 1) {
                    setRecordProperty(record, colNum, cellValue);
                } else {
                    validateColumnHeader(colNum, cellValue);
                }

                sb.append(!isFirst ? ", " : "").append("[COL-" + cellNum + "-" + colIndex + "]:")
                        .append(cellValue);

                isFirst = false;
            }

            // if (!hasNonEmptyValue) {
            //     accumulateError(rowNum, 0, "row can not be empty");
            //     logger.debug("the complete row can not be empty " + errors.toString());
            //     throw new VedantuException(VedantuErrorCode.STUDENT_FILE_UNPARSEABLE,
            //             errors.toString());
            // }

            if (rowNum > 1) {
                boolean ignoreError = false;
                if (hasNonEmptyValue) {
                    String key = !StringUtils.isEmpty(record.memberId) ? 
                            record.memberId.toUpperCase() : record.email.toLowerCase();
                    logger.debug("=========== rowKey: " + key + " ===============");
                    if (!StringUtils.isEmpty(key) && studentRecords.get(key) != null) {
                        accumulateError(rowNum, 0, "Duplicate record for MemberId = "
                                + record.memberId + ", email = " + record.email
                                + " already on row: memberId="
                                + studentRecords.get(key).get(0).memberId + ", email="
                                + record.email);
                    } else {

                        if (!StringUtils.isEmpty(key) && studentRecords.get(key) == null) {
                            studentRecords.put(key, new ArrayList<StudentXLRecord>());
                            previousKey = key;
                        } else if (StringUtils.isEmpty(key)) {
                            ignoreError = true;
                        }

                        if (previousKey != null) {
                            studentRecords.get(previousKey).add(record);
                        }
                    }
                    logger.debug("==== calling validate record  ignoreError: " + ignoreError
                            + ", record:" + record);
                    validateRecord(record, ignoreError);
                }
            } else {
                // check if no of columns provided are valids
                int expextedColmns = HEADERS.length
                        + (orgExtraInfoFields == null ? 0 : orgExtraInfoFields.size());
                if (expextedColmns != cellNum) {
                    if (errors == null) {
                        errors = new ArrayList<String>();
                    }

                    List<String> expextedColumnsName = new ArrayList<String>();
                    expextedColumnsName.addAll(Arrays.asList(HEADERS));
                    if (orgExtraInfoFields != null) {
                        for (InputFieldInfo iFieldInfo : orgExtraInfoFields) {
                            expextedColumnsName.add(iFieldInfo.name);
                        }
                    }
                    errors.add("Expected no of columns[" + expextedColmns + "], Found[" + cellNum
                            + "] \\n Expected Columns : "
                            +  expextedColumnsName.stream().collect(Collectors.joining(",")));
                }

                if (hasErrors()) {
                    logger.debug("will not parse further as there are errors in column headers : "
                            + errors.toString());
                    throw new VedantuException(VedantuErrorCode.STUDENT_FILE_UNPARSEABLE,
                            errors.toString());
                }
            }

            logger.debug(sb.toString());
        }

    }

    private static final String[] HEADERS = { "MemberId", "Member First Name", "Member Last Name",
            "Year", "Center", "Section", "Email", "Contact No", "Gender", "DOB", "Father Name",
            "Father Contact No", "Mother Name", "Mother Contact No", "Guardian Name",
            "Guardian Contact No", "Parent Email", "Point of Sale", "Seller Reference No" };

    private boolean validateColumnHeader(int colNum, String cellValue) {

        if (colNum > HEADERS.length) {
            // 1st verify if provided column name is in extra input fields provided by the
            // organization
            // The extra info input fields should be provided in order as they are added in the
            // organization signup forms
            int orgExtraColNum = colNum - HEADERS.length;

            if (orgExtraInfoFields != null && orgExtraColNum <= orgExtraInfoFields.size()) {
                InputFieldInfo iFieldInfo = orgExtraInfoFields.get(orgExtraColNum - 1);
                if (iFieldInfo != null && iFieldInfo.name != null
                        && !iFieldInfo.name.equalsIgnoreCase(cellValue)) {
                    accumulateError(1, colNum, "Expected header '" + iFieldInfo.name + "' found '"
                            + cellValue + "'");
                    return false;
                }
            } else {
                accumulateError(1, colNum, "Unknown column header '" + cellValue + "'");
                return false;
            }
        } else {
           /* if (!StringUtils.equalsIgnoreCase(
                    VedantuStringUtils.toCanonicalName(HEADERS[colNum - 1]),
                    VedantuStringUtils.toCanonicalName(cellValue.trim()))) {
                accumulateError(1, colNum, "Expected header '" + HEADERS[colNum - 1] + "' found '"
                        + cellValue + "'");
                return false;
            }*/
        	return false;
        }
        return true;
    }

    private void setRecordProperty(StudentXLRecord record, int colNum, String cellValue) {

        switch (colNum) {
        case 1:
            record.memberId = cellValue.toUpperCase();
            break;
        case 2:
            record.firstName = cellValue;
            break;
        case 3:
            record.lastName = cellValue;
            break;
        case 4:
            record.year = cellValue;
            break;
        case 5:
            record.center = cellValue;
            break;
        case 6:
            record.section = cellValue;
            break;
        case 7:
            record.email = cellValue;
            break;
        case 8:
            record.contactNumber = cellValue;
            break;
        case 9:
            record.gender = Gender.valueOfKey(cellValue);
            break;
        case 10:
            record.dob = cellValue;
            break;
        case 11:
            if (null == record.father) {
                record.father = new MemberParentInfo();
            }
            record.father.name = cellValue;
            break;
        case 12:
            if (null == record.father) {
                record.father = new MemberParentInfo();
            }
            record.father.contactNumber = cellValue;
            break;
        case 13:
            if (null == record.mother) {
                record.mother = new MemberParentInfo();
            }
            record.mother.name = cellValue;
            break;
        case 14:
            if (null == record.mother) {
                record.mother = new MemberParentInfo();
            }
            record.mother.contactNumber = cellValue;
            break;
        case 15:
            if (null == record.guardian) {
                record.guardian = new MemberParentInfo();
            }
            record.guardian.name = cellValue;
            break;
        case 16:
            if (null == record.guardian) {
                record.guardian = new MemberParentInfo();
            }
            record.guardian.contactNumber = cellValue;
            break;
        case 17:
            record.parentEmail = cellValue;
            break;
        case 18:
            record.pointOfSale = cellValue;
            break;
        case 19:
            record.sellerReferenceNo = cellValue;
            break;
        default:

            int orgExtraColNum = colNum - HEADERS.length;

            if (orgExtraInfoFields != null && orgExtraColNum <= orgExtraInfoFields.size()) {
                InputFieldInfo iFieldInfo = orgExtraInfoFields.get(orgExtraColNum - 1);
                record.addMemberExtraInfo(iFieldInfo.name, cellValue);
            }
            break;
        }
    }

    private void validateRecord(StudentXLRecord record, boolean ignoreError) {

        List<String> propertyErrors = new ArrayList<String>();
        if (!ignoreError && StringUtils.isEmpty(record.memberId)
                && StringUtils.isEmpty(record.email)) {
            propertyErrors.add("missing both MemberId and Email");
        }
        if (!ignoreError && StringUtils.isEmpty(record.firstName)) {
            propertyErrors.add("missing FirstName");
        }

        if (StringUtils.isEmpty(record.center)) {
            propertyErrors.add("missing Center");
        } else {
            centers.add(record.center);
        }

        if (StringUtils.isEmpty(record.section)) {
            propertyErrors.add("missing Section");
        } else {
            // center should have been populated before hand as ascertained by
            // column ordering
            sections.add(getCenterQualifiedSectionCode(record.center, record.section));
            //StringUtils.join(Arrays.asList(centerCode, sectionCode), CENTER_SECTION_SEPARATOR);
            //public static final String CENTER_SECTION_SEPARATOR = "/";
        }

        if (!ignoreError && (null == record.gender || Gender.UNKNOWN == record.gender)) {
            propertyErrors.add("missing Gender");
        }

        if (!ignoreError && !VedantuStringUtils.isValidDOB(record.dob)) {
            propertyErrors.add("invalid DOB");
        }

        if (!CollectionUtils.isEmpty(propertyErrors)) {
            accumulateError(rowNum, 0, propertyErrors.stream().collect(Collectors.joining(",")));
        }
    }

    private void accumulateError(int rowNum, int colNum, String errorMsg) {

        if (null == errors) {
            errors = new ArrayList<String>();
        }
        errors.add("At row=" + rowNum + ", col=" + colNum + " : " + errorMsg);
    }

    public boolean hasErrors() {

        return !CollectionUtils.isEmpty(errors);
    }

    public List<String> getErrors() {

        return errors;
    }

    public Map<String, List<StudentXLRecord>> getRecords() {

        return studentRecords;
    }

    public Set<String> getCenters() {

        return centers;
    }

    public Set<String> getSections() {

        return sections;
    }
    public static String getCenterQualifiedSectionCode(String centerCode, String sectionCode) {

        return Arrays.asList(centerCode, sectionCode).stream().collect(Collectors.joining(CENTER_SECTION_SEPARATOR));
    }
    public static void main(String[] argv) {

        System.out.println("==============================================");
        File file = new File("/disk1/mydocuments/projects/vedantu/modules/members/students.xlsx");
        String progId = "progId";
        StudentsXLParser parser = new StudentsXLParser(file.getName(), file, progId, null);
        System.out.println("hasErrors: " + parser.hasErrors());
        if (parser.hasErrors()) {
            //System.out.println(StringUtils.join(parser.getErrors(), "\n"));
        }
        System.out.println("records.size: " + parser.getRecords().size());
        System.out.println("----------------------------------------------");
        for (Map.Entry<String, List<StudentXLRecord>> entry : parser.getRecords().entrySet()) {
            System.out.println(entry.getValue());
        }
        System.out.println("==============================================");
    }

}
