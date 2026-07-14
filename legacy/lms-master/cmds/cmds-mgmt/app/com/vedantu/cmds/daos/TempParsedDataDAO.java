package com.vedantu.cmds.daos;

import org.bson.types.ObjectId;

import com.vedantu.cmds.models.TempParsedDATA;
import com.vedantu.mongo.VedantuBasicDAO;

public class TempParsedDataDAO extends
		VedantuBasicDAO<TempParsedDATA, ObjectId> {

	public static final TempParsedDataDAO INSTANCE = new TempParsedDataDAO();

	private TempParsedDataDAO() {
		super(TempParsedDATA.class);
	}

}
