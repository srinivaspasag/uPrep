package com.lms.component;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.Repo.EntityOperationStatusRepo;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.mongo.EntityOperationStatus;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.CmdsContentLinkType;
import com.lms.managers.AbstractContentManager;
import com.lms.models.*;
import com.lms.models.event.search.details.EntityPublishingDetails;
import com.lms.pojos.requests.EntityOperationStatusRes;
import com.lms.pojos.requests.GetEntityPublishingStatusReq;
import com.lms.pojos.requests.PublishReq;
import com.lms.pojos.responce.GetStatus;
import com.lms.pojos.responce.PublishRes;
import com.lms.repo.*;
import com.lms.repository.OrgSectionRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class CMDSLibraryManager extends AbstractContentManager {
	private static final Logger logger = LoggerFactory.getLogger(CMDSLibraryManager.class);
	@Autowired
	private CMDSVideoRepo cmdsVideoRepo;
	@Autowired
	private CMDSDocumentRepo cmdsDocumentRepo;
	@Autowired
	private CMDSModuleRepo cmdsModuleRepo;
	@Autowired
	private CMDSTestRepo cmdsTestRepo;
	@Autowired
	private CMDSAssignmentRepo cmdsAssignmentRepo;
	@Autowired
	private EntityOperationStatusRepo entityOperationStatusRepo;
	@Autowired
	private OrgSectionRepo orgSectionRepo;
	@Autowired
	private CMDSModuleManager cmdsModuleManager;

	public PublishRes publish(PublishReq request) {
		logger.debug("......in publish function ........");
		PublishRes response = new PublishRes();
		for (final SrcEntity publishableEntity : request.entities) {
			try {

				logger.debug("Publishing entity : " + publishableEntity);

				if (checkIfPublishInProgress(publishableEntity)) {
					response.addStatus(publishableEntity.id, VedantuErrorCode.PUBLISH_IN_PROGRESS.toString());
					continue;
				}
				VedantuBaseMongoModel baseMongoModel = null;
				boolean isReadyToPublished = false;
				if (publishableEntity.type == EntityType.CMDSVIDEO) {
					CMDSVideo cmdsVideo = cmdsVideoRepo.findById(publishableEntity.id).get();
					isReadyToPublished = cmdsVideo.completed;
					baseMongoModel = cmdsVideo;
				} else if (publishableEntity.type == EntityType.CMDSDOCUMENT) {
					CMDSDocument cmdsDocument = cmdsDocumentRepo.findById(publishableEntity.id).get();
					isReadyToPublished = cmdsDocument.completed;
					baseMongoModel = cmdsDocument;
				} else if (publishableEntity.type == EntityType.CMDSMODULE) {
					CMDSModule cmdsModule = cmdsModuleRepo.findById(publishableEntity.id).get();
					isReadyToPublished = cmdsModule.completed;
				} else if (publishableEntity.type == EntityType.CMDSTEST) {
					CMDSTest cmdsTest = cmdsTestRepo.findById(publishableEntity.id).get();
					isReadyToPublished = cmdsTest.completed;
					baseMongoModel = cmdsTest;
				} else if (publishableEntity.type == EntityType.CMDSASSIGNMENT) {
					CMDSAssignment cmdsAssignment = cmdsAssignmentRepo.findById(publishableEntity.id).get();
					isReadyToPublished = cmdsAssignment.completed;
					baseMongoModel = cmdsAssignment;
				}
				if (baseMongoModel != null) {
					if (!isReadyToPublished) {
						throw new VedantuException(VedantuErrorCode.INCOMPLETE_PUBLISHABLE_STATE);
					}

					logger.debug("Entity need to be published using publisher " + baseMongoModel);

					final EntityOperationStatus jobStatus = new EntityOperationStatus();
					jobStatus.type = publishableEntity.type;
					jobStatus.id = publishableEntity.id;
					jobStatus.numOfSteps++;
					jobStatus.numOfSteps++; // for entity size calculations
					entityOperationStatusRepo.save(jobStatus);
					logger.debug("Entity will be  published in jobId " + jobStatus._getStringId());
					EntityPublishingDetails entityPublishingDetails = new EntityPublishingDetails(request.userId,
							request.orgId, publishableEntity, jobStatus._getStringId());
					generateEventAysc(request.userId, entityPublishingDetails, EventType.PUBLISH_ENTITY);
					addPublishEntityProgress(publishableEntity);
					response.addStatus(publishableEntity.id, jobStatus._getStringId());
				} else {
					throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_PUBLISHED);
				}
			} catch (VedantuException exception) {
				response.addStatus(publishableEntity.id, null, exception.errorCode);
			}

		}
		return response;

	}

	private boolean checkIfPublishInProgress(SrcEntity publishableEntity) {
		// TODO Auto-generated method stub
		if (publishableEntity.type == EntityType.CMDSVIDEO) {
			return cmdsVideoRepo.findById(publishableEntity.id).get().publishingInProgress;
		} else if (publishableEntity.type == EntityType.CMDSDOCUMENT) {
			return cmdsDocumentRepo.findById(publishableEntity.id).get().publishingInProgress;
		} else if (publishableEntity.type == EntityType.CMDSMODULE) {
			return cmdsModuleRepo.findById(publishableEntity.id).get().publishingInProgress;
		} else if (publishableEntity.type == EntityType.CMDSTEST) {
			return cmdsTestRepo.findById(publishableEntity.id).get().publishingInProgress;
		} else if (publishableEntity.type == EntityType.CMDSASSIGNMENT) {
			return cmdsAssignmentRepo.findById(publishableEntity.id).get().publishingInProgress;
		}
		return false;
	}

	private void addPublishEntityProgress(SrcEntity publishableEntity) {
		// TODO Auto-generated method stub
		if (publishableEntity.type == EntityType.CMDSVIDEO) {
			CMDSVideo cmdsVideo = cmdsVideoRepo.findById(publishableEntity.id).get();
			cmdsVideo.publishingInProgress = true;
			cmdsVideoRepo.save(cmdsVideo);
		} else if (publishableEntity.type == EntityType.CMDSDOCUMENT) {
			CMDSDocument cmdsDocument = cmdsDocumentRepo.findById(publishableEntity.id).get();
			cmdsDocument.publishingInProgress = true;
			cmdsDocumentRepo.save(cmdsDocument);
		} else if (publishableEntity.type == EntityType.CMDSMODULE) {
			CMDSModule cmdsModule = cmdsModuleRepo.findById(publishableEntity.id).get();
			cmdsModule.publishingInProgress = true;
			cmdsModuleRepo.save(cmdsModule);
		} else if (publishableEntity.type == EntityType.CMDSTEST) {
			CMDSTest cmdsTest = cmdsTestRepo.findById(publishableEntity.id).get();
			cmdsTest.publishingInProgress = true;
			cmdsTestRepo.save(cmdsTest);
		} else if (publishableEntity.type == EntityType.CMDSASSIGNMENT) {
			CMDSAssignment cmdsAssignment = cmdsAssignmentRepo.findById(publishableEntity.id).get();
			cmdsAssignment.publishingInProgress = true;
			cmdsAssignmentRepo.save(cmdsAssignment);
		}
	}

	public GetStatus getStatus(GetEntityPublishingStatusReq request) {
		List<EntityOperationStatus> statuses = entityOperationStatusRepo.findById(request.jobIds);
		if (CollectionUtils.isEmpty(statuses)) {
			throw new VedantuException(VedantuErrorCode.INVALID_JOB_ID);
		}

		GetStatus response = new GetStatus();

		for (EntityOperationStatus status : statuses) {
			EntityOperationStatusRes individualResponse = new EntityOperationStatusRes();
			logger.debug("individual reponse is" + individualResponse);
			individualResponse.jobId = status._getStringId();
			individualResponse.id = status.id;
			individualResponse.type = status.type;
			individualResponse.numCompletedSteps = status.numOfStepsCompleted;
			individualResponse.numOfSteps = status.numOfSteps;
			individualResponse.errorCode = status.errorCode;
			//individualResponse.message = status.message;
			if (StringUtils.isEmpty(status.errorCode)) {
				individualResponse.errorCode = "";
			}

			response.list.add(individualResponse);

		}
		response.totalHits = statuses.size();
		return response;

	}

	public   int getAllProgramsAddedTo(SrcEntity content, CmdsContentLinkType linkType) {

		// TODO update it use only sectionIds to optimize using aggregation
		logger.info(" Getting programs added to information ");
		AtomicLong totalHits = new AtomicLong();
		List<CMDSContentLink> links = cmdsModuleManager.getCmdsContentLinks(content,
				new SrcEntity(EntityType.SECTION, null), linkType, null, 0, Integer.MAX_VALUE,
				VedantuRecordState.ACTIVE, totalHits);

		Set<String> programIds = new HashSet<String>();
		if (!CollectionUtils.isEmpty(links)) {
			for (CMDSContentLink link : links) {
				OrgSection section =orgSectionRepo.findById(link.target.id).get();
				if (section != null && !programIds.contains(section.programId)) {
					programIds.add(section.programId);
				}

			}
		}

		return programIds.size();
	}


}
