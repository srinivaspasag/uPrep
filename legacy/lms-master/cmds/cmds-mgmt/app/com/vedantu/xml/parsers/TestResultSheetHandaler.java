package com.vedantu.xml.parsers;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.poi.xssf.model.SharedStringsTable;

import com.vedantu.cmds.daos.TempParsedDataDAO;
import com.vedantu.cmds.models.TempParsedDATA;
import com.vedantu.commons.VedantuException;
import com.vedantu.xml.parsers.models.AbstractXMLDataModel;

public class TestResultSheetHandaler extends AbstractSheetHandaler {

	public TestResultSheetHandaler() {
		this(null);
	}

	public TestResultSheetHandaler(SharedStringsTable sst) {
		super(sst);
	}

	@Override
	public void persistModel(Map<String, String> data, int rowNo) {
		super.persistModel(data, rowNo);
		if (MapUtils.isEmpty(data)) {
			return;
		}
		TempParsedDATA tempData = new TempParsedDATA(uuid, rowNo, sheetId,
				sheetName, data);
		TempParsedDataDAO.INSTANCE.save(tempData);
	}

	@Override
	public void validateModel(AbstractXMLDataModel model)
			throws VedantuException {
		super.validateModel(model);
	}

}
