package com.vedantu.cmds.factory;

import java.util.HashMap;
import java.util.Map;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.mgmt.interfaces.IContentExporter;
import com.vedantu.commons.enums.EntityType;

public class ContentExporterFactory {

    private static final ALogger          LOGGER                 = Logger.of(ContentExporterFactory.class);
    public static ContentExporterFactory  INSTANCE               = new ContentExporterFactory();

    private Map<String, IContentExporter> contentExporterFactory = new HashMap<String, IContentExporter>();

    private ContentExporterFactory() {

    }

    public boolean register(EntityType entityType, IContentExporter basicExporter) {

        contentExporterFactory.put(entityType.name(), basicExporter);
        return true;

    }

    public IContentExporter get(EntityType entityType) {

        LOGGER.debug("Getting basic information : " + entityType.name());
        if (contentExporterFactory.containsKey(entityType.name())) {
            LOGGER.debug("Found Exporter information : " + entityType.name());
            return contentExporterFactory.get(entityType.name());
        }

        LOGGER.debug("No Exporter found information : " + entityType.name());
        return null;

    }

}
