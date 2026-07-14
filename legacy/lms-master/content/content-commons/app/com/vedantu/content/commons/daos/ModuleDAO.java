package com.vedantu.content.commons.daos;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.content.models.Module;
import com.vedantu.mongo.VedantuBasicDAO;

public class ModuleDAO extends VedantuBasicDAO<Module, ObjectId>{

	private static final ALogger            LOGGER   = Logger.of(ModuleDAO.class);

    public static final ModuleDAO INSTANCE = new ModuleDAO();

    private ModuleDAO() {
        super(Module.class);
    }
}
