package com.lms.repo;

import com.lms.models.CMDSFolder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CMDSFolderRepo extends MongoRepository<CMDSFolder, String> {
	CMDSFolder findByIdAndOrganizationId(String folderId, String orgId);

	CMDSFolder findByOrganizationIdAndParentAndName(String orgId, String parentId, String name);

	CMDSFolder findByParentSources(List<String> childsOfCurrentFolder);

	CMDSFolder findByParentSources(String folderId);

	List<CMDSFolder> findByParent(String folderId);
}
