package com.vedantu.cmds.daos;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Criteria;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.google.code.morphia.query.UpdateResults;
import com.vedantu.cmds.mgmt.interfaces.ICMDSResource;
import com.vedantu.cmds.models.CMDSFolder;
import com.vedantu.cmds.models.event.search.details.CMDSResourceDetails;
import com.vedantu.cmds.pojos.content.question.CMDSFolderInfo;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;

public class CMDSFolderDAO extends VedantuBasicDAO<CMDSFolder, ObjectId> implements ICMDSResource {

    private static final ALogger      LOGGER   = Logger.of(CMDSFolderDAO.class);
    public static final CMDSFolderDAO INSTANCE = new CMDSFolderDAO();

    public CMDSFolderDAO() {

        super(CMDSFolder.class);
    }

    public String doesRootFolderExists(String orgId) {

        CMDSFolder cmdsFolder = getRootFolder(orgId);
        if (cmdsFolder != null) {
            return cmdsFolder._getStringId();
        }
        return null;
    }

    public CMDSFolder getRootFolder(String orgId) {

        Query<CMDSFolder> query = ds.createQuery(CMDSFolder.class);

        List<Criteria> list = new ArrayList<Criteria>();
        list.add(query.criteria("organizationId").equal(orgId));
        list.add(query.criteria("isRoot").equal(Boolean.TRUE));
        list.add(query.criteria("parent").doesNotExist());
        list.add(query.criteria("parent").notEqual(""));
        query.and(list.toArray(new Criteria[] {}));
        return query.get();
    }

    public CMDSFolder findById(String orgId, String folderId) {

        // TODO business logic requirement
        CMDSFolder folder = getDS().find(CMDSFolder.class).filter("id", new ObjectId(folderId))
                .filter("organizationId", orgId).get();

        return folder;
    }

    /**
     * Create folders if there is no folder of same name in parent
     * 
     * @param parentId
     * @param userId
     * @param organizationId
     * @param name
     * @return
     * @throws VedantuException
     */
    public CMDSFolder createFolder(String parentId, String userId, String organizationId,
            String name) throws VedantuException {

        List<String> parents = null;

        if (StringUtils.isNotEmpty(parentId)) {

            CMDSFolder parentFolder = findById(organizationId, parentId);

            if (parentFolder == null) {
                throw new VedantuException(VedantuErrorCode.PARENT_DIRECTORY_NOT_FOUND);
            }
            LOGGER.debug("Found parent directory:" + parentFolder);
            if (parentFolder != null) {
                parents = parentFolder.parentSources;
                if (CollectionUtils.isEmpty(parents)) {
                    parents = new ArrayList<String>();
                }
                parents.add(0, parentFolder._getStringId());
            }
        }

        if (checkForSimilarNameDirectory(organizationId, parentId, name)) {
            throw new VedantuException(VedantuErrorCode.FOLDER_ALREADY_EXISTS);
        }

        CMDSFolder directory = new CMDSFolder();
        directory.setName(name);
        directory.parentSources = parents;
        directory.organizationId = organizationId;
        directory.parent = parentId;
        directory.userId = userId;
        if (CollectionUtils.isEmpty(parents) && StringUtils.isEmpty(parentId)) {
            directory.isRoot = true;
        }

        save(directory);

        LOGGER.debug("default folder created");
        return directory;
    }

    public boolean checkForSimilarNameDirectory(String orgId, String parentId, String name) {

        // TODO business logic requirement
        CMDSFolder directory = getDS().find(CMDSFolder.class).filter("organizationId", orgId)
                .filter("parent", parentId).filter("name", name).get();
        if (directory != null) {
            return true;
        }
        return false;
    }

    @Override
    public CMDSResourceDetails getCMDSResourceDetails(VedantuBaseMongoModel model) {

        CMDSFolder folder = (CMDSFolder) model;
        CMDSResourceDetails details = new CMDSResourceDetails();
        details.fromMongoModel(model);
        details.contentSrc = new SrcEntity(EntityType.ORGANIZATION, folder.organizationId);
        details.content = new SrcEntity(EntityType.FOLDER, folder._getStringId());
        details.queryContext = "";
        details.name = folder.name;
        return details;
    }

    public boolean annotateParentInfo(CMDSFolderInfo folderInfo, String folderId) {

        if (folderInfo == null || folderId == null || !folderInfo.id.equalsIgnoreCase(folderId)) {
            return false;
        }
        CMDSFolder folder = CMDSFolderDAO.INSTANCE.getById(folderId);
        if (folder == null) {
            return false;
        }
        if (CollectionUtils.isNotEmpty(folder.parentSources)) {
            if (folderInfo.parents == null) {
                folderInfo.parents = new ArrayList<CMDSFolderInfo>();
            }
            for (String parent : folder.parentSources) {
                CMDSFolderInfo info = this.getBasicInfo(parent);
                folderInfo.parents.add(0, info);
            }
        }
        return true;
    }

    public boolean updateParent(String folderId, String newParent, List<String> newParents,
            List<String> oldParents) {

        Query<CMDSFolder> folderIdQuery = CMDSFolderDAO.INSTANCE.getQuery();
        folderIdQuery.filter(FIELD_ID, new ObjectId(folderId));
        UpdateOperations<CMDSFolder> updateQuery = CMDSFolderDAO.INSTANCE.createUpdateOperations();

        updateQuery.set("parent", newParent);
        newParents.add(0, newParent);
        updateQuery.set("parentSources", newParents);
        LOGGER.debug("Finding moving folder query " + folderIdQuery.toString());
        LOGGER.debug("Update moving folder's parent " + updateQuery.toString());

        UpdateResults<CMDSFolder> updateResults = CMDSFolderDAO.INSTANCE.update(folderIdQuery,
                updateQuery);
        if (updateResults.getError() != null) {
            LOGGER.debug("Error occrured while updating parent folders for " + folderId
                    + " Error: " + updateResults.getError());
        }

        UpdateOperations<CMDSFolder> removeOldParentsQuery = CMDSFolderDAO.INSTANCE
                .createUpdateOperations();
        Query<CMDSFolder> childFoldersFindingQuery = CMDSFolderDAO.INSTANCE.getQuery();
        if (CollectionUtils.isNotEmpty(oldParents)) {
            List<String> childsOfCurrentFolder = new ArrayList<String>();
            childsOfCurrentFolder.add(folderId);
            childsOfCurrentFolder.addAll(oldParents);
            childFoldersFindingQuery.field("parentSources").hasAllOf(childsOfCurrentFolder);

            LOGGER.debug("Removing oldParents form moving folder's childs " + oldParents);
            removeOldParentsQuery.removeAll("parentSources", oldParents);

            LOGGER.debug("Finding moving folder's childs of parent update "
                    + childFoldersFindingQuery.toString());
            LOGGER.debug("Updating moving folder's childs parents query "
                    + removeOldParentsQuery.toString());
            updateResults = CMDSFolderDAO.INSTANCE.update(childFoldersFindingQuery,
                    removeOldParentsQuery);

            if (updateResults.getError() != null) {
                LOGGER.debug("Error occrured while removing older parent source hiearchy of folders "
                        + folderId + " Error: " + updateResults.getError());
            }
        }
        if (CollectionUtils.isNotEmpty(newParents)) {
            UpdateOperations<CMDSFolder> updateNewParentsInChildsQUery = CMDSFolderDAO.INSTANCE
                    .createUpdateOperations();
            Query<CMDSFolder> currentFoldersChildQuery = CMDSFolderDAO.INSTANCE.getQuery();
            // newParents.add(folderId); // adding current node to parents for existing one
            currentFoldersChildQuery.field("parentSources").hasThisOne(folderId);
            updateNewParentsInChildsQUery.addAll("parentSources", newParents, false);
            LOGGER.debug("Finding childs of moving folders " + currentFoldersChildQuery.toString());
            LOGGER.debug("Updating new parents to childs of moving folders "
                    + updateNewParentsInChildsQUery.toString());
            updateResults = CMDSFolderDAO.INSTANCE.update(currentFoldersChildQuery,
                    updateNewParentsInChildsQUery);
            if (updateResults.getError() != null) {
                LOGGER.debug("Error occrured while updating childs of moved folder for " + folderId
                        + " Error: " + updateResults.getError());
            }
        }
        return true;
    }

    public List<CMDSFolder> getChilds(String folderId, int start, int size, MutableLong totals) {

        Query<CMDSFolder> childQuery = CMDSFolderDAO.INSTANCE.createQuery();
        childQuery.field("parent").equal(folderId);
        childQuery.order("cName");
        totals.setValue(childQuery.countAll());
        return childQuery.offset(start).limit(size).asList();

    }

}
