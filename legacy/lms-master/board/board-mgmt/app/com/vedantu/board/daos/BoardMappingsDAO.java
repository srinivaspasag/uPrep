package com.vedantu.board.daos;

import java.util.List;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.vedantu.board.models.BoardMapping;
import com.vedantu.mongo.VedantuBasicDAO;

public class BoardMappingsDAO extends VedantuBasicDAO<BoardMapping, ObjectId> {

    private static final ALogger LOGGER       = Logger.of(BoardMappingsDAO.class);

    public static final BoardMappingsDAO INSTANCE = new BoardMappingsDAO();

    private BoardMappingsDAO() {
        super(BoardMapping.class);
    }

    public List<BoardMapping> getByParentOrgId(String orgId) {
        Query<BoardMapping> mappings = getDS().createQuery(BoardMapping.class);
        mappings.filter("parentOrgId", orgId);
        return mappings.asList();
    }

    public BoardMapping getBySharedToOrgId(String parentOrgId, String sharedToOrgId) {
        Query<BoardMapping> mappings = getDS().createQuery(BoardMapping.class);
        if(!parentOrgId.equals("N.A")){
            mappings.filter("parentOrgId", parentOrgId);
        }
        mappings.filter("sharedToOrgId", sharedToOrgId);
        return mappings.get();
    }

    public boolean saveBoardMappings(BoardMapping boardMapping){
        save(boardMapping);
        return true;
    }

}
