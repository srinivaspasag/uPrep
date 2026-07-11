package com.lms.services.serviceImpl;


import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.models.Module;
import com.lms.models.*;
import com.lms.models.tests.Assignment;
import com.lms.pojos.*;
import com.lms.pojos.requests.*;
import com.lms.pojos.responce.GetScheduleRes;
import com.lms.repository.*;
import com.lms.services.ClassroomConnectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
public class ClassroomConnectServiceImpl implements ClassroomConnectService {
    private static final Logger logger = LoggerFactory.getLogger(ClassroomConnectServiceImpl.class);
    @Autowired
    private ScheduleRepo scheduleRepo;
    @Autowired
    private TestRepo testRepo;
    @Autowired
    private ModuleRepo moduleRepo;
    @Autowired
    private VideoRepo videoRepo;
    @Autowired
    private DocumentsRepo documentsRepo;
    @Autowired
    private AssignmentRepo assignmentRepo;
    @Autowired
    private UserEntityAttemptRepo userEntityAttemptRepo;
    @Override
    public VedantuResponse getschedule(GetScheduleReq getScheduleReq) {
        if (getScheduleReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetScheduleRes getScheduleRes = getSchedule(getScheduleReq);
        return new VedantuResponse(getScheduleRes);
    }

    @Override
    public VedantuResponse removedaySchedule(GetScheduleReq removeScheduleReq) {
        if (removeScheduleReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        SaveScheduleRes removeContentLinksRes = removeDaySchedule(removeScheduleReq);
        return new VedantuResponse(removeContentLinksRes);
    }

    @Override
    public VedantuResponse removeschedule(RemoveScheduleReq removeScheduleReq) {
        if (removeScheduleReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        SaveScheduleRes removeContentLinksRes = removeSchedule(removeScheduleReq);
        return new VedantuResponse(removeContentLinksRes);
    }

    @Override
    public VedantuResponse getDayschedule(GetScheduleReq getScheduleReq) {
        if (getScheduleReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetDayScheduleRes getScheduleRes = getDaySchedule(getScheduleReq);
        return new VedantuResponse(getScheduleRes);
    }

    @Override
    public VedantuResponse addschedule(AddScheduleReq addScheduleReq) {
        if (addScheduleReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        SaveScheduleRes response = addSchedule(addScheduleReq);
        return new VedantuResponse(response);
    }

    private GetDayScheduleRes getDaySchedule(GetScheduleReq getScheduleReq) {
        if(getScheduleReq.day == 0){
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "Missing param DAY");
        }
        GetDayScheduleRes res = new GetDayScheduleRes();
        Schedule schedule = getScheduleByDate(
                getScheduleReq.orgId, getScheduleReq.sectionId, getScheduleReq.month,
                getScheduleReq.day);
        for(SubjectMetadata metadata : schedule.metadata){
            SubMetadata subMetadata = new SubMetadata();
            subMetadata.id = metadata.id;
            subMetadata.subName = metadata.name;
            for(EntityDetails detail : metadata.details){
                EntityTypeData det = new EntityTypeData();
                det.type = detail.type;
                for(String cmdsId : detail.cmdsIds){
                    EntityList list = new EntityList();
                    list.id = getGlobalId(detail.type, cmdsId);
                    if(list.id == null){
                        throw new VedantuException(VedantuErrorCode.ENTITY_NOT_FOUND, "Entity not found");
                    }
                    list.cmdsId = cmdsId;
                    String name = getName(list.id, detail.type);
                    list.name = name;
                    if(detail.type.equals("TEST")){
                        list.attempted = getAttempt(getScheduleReq.userId, EntityType.TEST, list.id) != null;
                    }
                    det.contents.add(list);
                }
                subMetadata.details.add(det);
            }
            res.metadata.add(subMetadata);
        }
        return res;
    }

    private SaveScheduleRes removeSchedule(RemoveScheduleReq removeScheduleReq) {
        if (removeScheduleReq.day == 0) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "Missing param DAY");
        }
        SaveScheduleRes res = new SaveScheduleRes();
        Schedule schedule = getScheduleByDate(
                removeScheduleReq.orgId, removeScheduleReq.sectionId, removeScheduleReq.month,
                removeScheduleReq.day);
        if (schedule == null)
            throw new VedantuException(VedantuErrorCode.INVALID_CODE, "scheduleNotFound");
        Iterator<SubjectMetadata> metadata = schedule.metadata.iterator();
        List<SubjectMetadata> newMetadata = new ArrayList<SubjectMetadata>();
        while (metadata.hasNext()) {
            SubjectMetadata subMetaData = metadata.next();
            if (subMetaData.id.equals(removeScheduleReq.boardId)) {
                Iterator<EntityDetails> details = subMetaData.details.iterator();
                List<EntityDetails> newDetails = new ArrayList<EntityDetails>();
                while (details.hasNext()) {
                    EntityDetails detail = details.next();
                    if (detail.type.equals(removeScheduleReq.entityType)) {
                        if(detail.ids.contains(removeScheduleReq.entityId) && detail.cmdsIds.contains(removeScheduleReq.entityCmdsId)){
                            detail.ids.remove(removeScheduleReq.entityId);
                            detail.cmdsIds.remove(removeScheduleReq.entityCmdsId);
                        }
                        if(detail.cmdsIds.size() == 0 && detail.ids.size() == 0){
                            details.remove();
                        }else{
                            newDetails.add(detail);
                        }
                    }else{
                        newDetails.add(detail);
                    }
                }
                subMetaData.details = newDetails;
                if(subMetaData.details.size() == 0){
                    metadata.remove();
                    schedule.boardIds.remove(subMetaData.id);
                }else{
                    newMetadata.add(subMetaData);
                }
            }else{
                newMetadata.add(subMetaData);
            }
        }
        schedule.metadata = newMetadata;
       scheduleRepo.save(schedule);
        res.saved = true;
        return res;

    }

    private SaveScheduleRes removeDaySchedule(GetScheduleReq removeScheduleReq) {
        if(removeScheduleReq.day == 0){
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "Missing param DAY");
        }
        SaveScheduleRes res = new SaveScheduleRes();
        res.saved = removeScheduleByDate(
                removeScheduleReq.orgId, removeScheduleReq.sectionId, removeScheduleReq.month,
                removeScheduleReq.day);
        return res;
    }

    public boolean removeScheduleByDate(String orgId, String sectionId, long month, long day) {
        Schedule removeScheduleQuery = getScheduleByDate(orgId, sectionId, month, day);
        if(removeScheduleQuery == null) {
            return false;
        }else{
            removeScheduleQuery.recordState = VedantuRecordState.DELETED;
            scheduleRepo.save(removeScheduleQuery);
            return true;
        }
    }

    private GetScheduleRes getSchedule(GetScheduleReq getScheduleReq) {
        GetScheduleRes res = new GetScheduleRes();
        List<Schedule> scheduleList = getScheduleByMonth(getScheduleReq.getOrgId(), getScheduleReq.getSectionId(), getScheduleReq.getMonth());
        res.month = getScheduleReq.month;
        Iterator<Schedule> schedules = scheduleList.iterator();
        Schedule schedule = new Schedule();
        List<ScheduleDay> scheduleDays = new ArrayList<ScheduleDay>();
        while(schedules.hasNext()){
            schedule = schedules.next();
            ScheduleDay scheduleDay = new ScheduleDay();
            scheduleDay.date = schedule.day;
            List<DayMetadata> metadata = new ArrayList<DayMetadata>();
            for(SubjectMetadata subMetaData : schedule.metadata) {
                List<EntityCount> entityDetails = new ArrayList<EntityCount>();
                DayMetadata dayMetaData = new DayMetadata();
                dayMetaData.id = subMetaData.id;
                dayMetaData.name = subMetaData.name;
                for(EntityDetails details : subMetaData.details){
                    EntityCount entityDetail = new EntityCount();
                    entityDetail.type = details.type;
                    entityDetail.count = details.ids.size();
                    entityDetails.add(entityDetail);
                }
                dayMetaData.details = entityDetails;
                metadata.add(dayMetaData);
            }
            scheduleDay.metadata = metadata;
            scheduleDays.add(scheduleDay);
        }
        res.days = scheduleDays;
        return res;
    }
    public List<Schedule> getScheduleByMonth(String orgId, String sectionId, long month) {
          List<Schedule> schedules=scheduleRepo.findByOrgIdAndSectionIdAndMonthAndRecordState(orgId,sectionId,month, VedantuRecordState.ACTIVE);
          return schedules;
    }
    public Schedule getScheduleByDate(String orgId, String sectionId, long month, long day) {

        List<Schedule> schedules=scheduleRepo.findByOrgIdAndSectionIdAndMonthAndRecordStateAndDay(orgId,sectionId,month, VedantuRecordState.ACTIVE,day);
        Iterator<Schedule> schedule = schedules.iterator();
        while(schedule.hasNext()){
            return schedule.next();
        }
        return null;
    }
    public  String getGlobalId(String type, String cmdsId){
        if(type.equals("TEST")){
            return getGlobalId(new SrcEntity(EntityType.CMDSTEST, cmdsId));
        }else if(type.equals("VIDEO")){
            return getGlobalId(new SrcEntity(EntityType.CMDSVIDEO, cmdsId));
        }else if(type.equals("MODULE")){
            return getGlobalId(new SrcEntity(EntityType.CMDSMODULE, cmdsId));
        }else if(type.equals("DOCUMENT")){
            return getGlobalId(new SrcEntity(EntityType.CMDSDOCUMENT, cmdsId));
        }else{
            return null;
        }
    }

    public  String getGlobalId(SrcEntity entity){
        if(entity.type == EntityType.CMDSTEST){
            Test test = getByCMDSTestId(entity.id);
            if(test == null){
                return null;
            }else{
                return test._getStringId();
            }
        }else if(entity.type == EntityType.CMDSMODULE){
            Module module = moduleRepo.findByCmdsModuleId(entity.getId());
            if(module == null){
                return null;
            }else{
                return module._getStringId();
            }
        }else if(entity.type == EntityType.CMDSVIDEO){
            Video video = videoRepo.findByCmdsVideoId(entity.getId());
            if(video == null){
                return null;
            }else{
                return video._getStringId();
            }
        }else if(entity.type == EntityType.CMDSDOCUMENT){
            Documents doc = documentsRepo.findByCmdsDocId(entity.getId());
            if(doc == null){
                return null;
            }else{
                return doc._getStringId();
            }
        }else if(entity.type == EntityType.CMDSASSIGNMENT){
            Assignment assignment = assignmentRepo.findByCmdsId(entity.getId());
            if(assignment == null){
                return null;
            }else{
                return assignment._getStringId();
            }
        }else{
            return null;
        }
    }
    public Test getByCMDSTestId(String cmdsTestId) {

        Test test = testRepo.findByCmdsTestId(cmdsTestId);

        if (test == null) {
            logger.error("Cannot find test with the cmds test id :" + cmdsTestId);
        }

        return test;
    }
    private  String getName(String id, String type) {
        if(type.equals(EntityType.TEST.name())){
            Optional<Test> test = testRepo.findById(id);
            if(!test.isPresent()){
                return null;
            }else{
                return test.get().getName();
            }
        }else if(type.equals(EntityType.MODULE.name())){
            Optional<Module> module = moduleRepo.findById(id);
            if(!module.isPresent()){
                return null;
            }else{
                return module.get().getName();
            }
        }else if(type.equals(EntityType.VIDEO.name())){
            Optional<Video> video = videoRepo.findById(id);
            if(!video.isPresent()){
                return null;
            }else{
                return video.get().getName();
            }
        }else if(type.equals(EntityType.DOCUMENT.name())){
            Optional<Documents> doc = documentsRepo.findById(id);
            if(!doc.isPresent()){
                return null;
            }else{
                return doc.get().getName();
            }
        }else if(type.equals(EntityType.ASSIGNMENT.name())){
            Optional<Assignment> assignment = assignmentRepo.findById(id);
            if(!assignment.isPresent()){
                return null;
            }else{
                return assignment.get().getName();
            }
        }else{
            return null;
        }
    }
    public UserEntityAttempt getAttempt(String userId, EntityType entityType, String entityId) {

        logger.debug("getAttempt userId: " + userId + ", entityType: " + entityType + ", entityId"
                + entityId);

        UserEntityAttempt userEntityAttempt = userEntityAttemptRepo.findByUserIdAndEntityTypeAndEntityId(userId,entityType,entityId);

        logger.info("getAttempt userEntityAttempt: " + userEntityAttempt);

        return userEntityAttempt;
    }
    public  SaveScheduleRes addSchedule(AddScheduleReq addScheduleReq) throws VedantuException {
        SaveScheduleRes response = new SaveScheduleRes();
        if(addScheduleReq.entityList.isEmpty()){
            throw new VedantuException(VedantuErrorCode.NO_CONTENT_FOUND, "No content selected");
        }
        List<String> cmdsTestIds = new ArrayList<String>();
        List<String> cmdsModuleIds = new ArrayList<String>();
        List<String> cmdsDocumentIds = new ArrayList<String>();
        List<String> cmdsVideoIds = new ArrayList<String>();
        List<String> cmdsAssignmentIds = new ArrayList<String>();

        List<String> testIds = new ArrayList<String>();
        List<String> moduleIds = new ArrayList<String>();
        List<String> documentIds = new ArrayList<String>();
        List<String> videoIds = new ArrayList<String>();
        List<String> assignmentIds = new ArrayList<String>();
        for(SrcEntity entity : addScheduleReq.entityList){
            if(!StringUtils.isEmpty(entity.id)){
                if(entity.type == EntityType.CMDSTEST){
                    cmdsTestIds.add(entity.id);
                    String id = getGlobalId(new SrcEntity(EntityType.CMDSTEST, entity.id));
                    if(!StringUtils.isEmpty(id)){
                        testIds.add(id);
                    }else{
                        throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED, "One or More than one content is not published");
                    }
                }else if(entity.type == EntityType.CMDSMODULE){
                    cmdsModuleIds.add(entity.id);
                    String id = getGlobalId(new SrcEntity(EntityType.CMDSMODULE, entity.id));
                    if(!StringUtils.isEmpty(id)){
                        moduleIds.add(id);
                    }else{
                        throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED, "One or More than one content is not published");
                    }
                }else if(entity.type == EntityType.CMDSVIDEO){
                    cmdsVideoIds.add(entity.id);
                    String id = getGlobalId(new SrcEntity(EntityType.CMDSVIDEO, entity.id));
                    if(!StringUtils.isEmpty(id)){
                        videoIds.add(id);
                    }else{
                        throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED, "One or More than one content is not published");
                    }
                }else if(entity.type == EntityType.CMDSDOCUMENT){
                    cmdsDocumentIds.add(entity.id);
                    String id = getGlobalId(new SrcEntity(EntityType.CMDSDOCUMENT, entity.id));
                    if(!StringUtils.isEmpty(id)){
                        documentIds.add(id);
                    }else{
                        throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED, "One or More than one content is not published");
                    }
                }else if(entity.type == EntityType.CMDSASSIGNMENT){
                    cmdsAssignmentIds.add(entity.id);
                    String id = getGlobalId(new SrcEntity(EntityType.CMDSASSIGNMENT, entity.id));
                    if(!StringUtils.isEmpty(id)){
                        assignmentIds.add(id);
                    }else{
                        throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED, "One or More than one content is not published");
                    }
                }else{
                    throw new VedantuException(VedantuErrorCode.NOT_ALLOWED, "One or More than one content is not supported");
                }
            }else{
                throw new VedantuException(VedantuErrorCode.NO_CONTENT_FOUND, "One or More than one content id is not found");
            }
        }
        Schedule schedule = getScheduleByDate(addScheduleReq.orgId, addScheduleReq.sectionId, addScheduleReq.month, addScheduleReq.day);
        if(schedule != null){
            List<String> boardIds = schedule.boardIds;
            if(boardIds.contains(addScheduleReq.boardId)){
                SubjectMetadata subMetaData = null;
                for(SubjectMetadata singleMetaData : schedule.metadata){
                    if(addScheduleReq.boardId.equals(singleMetaData.id)){
                        subMetaData = singleMetaData;
                        break;
                    }
                }
                if(subMetaData == null){
                    throw new VedantuException(VedantuErrorCode.NO_SUBJECT_FOUND, "Subject not found");
                }
                List<EntityDetails> details = subMetaData.details;
                if(details == null){
                    details = new ArrayList<EntityDetails>();
                }
                if(!cmdsTestIds.isEmpty() && !testIds.isEmpty()){
                    boolean flag = false;
                    for(EntityDetails detail : details){
                        if(detail.type.equals(EntityType.TEST.name())){
                            Iterator<String> cmdstestIds = cmdsTestIds.iterator();
                            while(cmdstestIds.hasNext()){
                                String cmdsId = cmdstestIds.next();
                                if(detail.cmdsIds.contains(cmdsId)){
                                    String globalId = getGlobalId(new SrcEntity(EntityType.CMDSTEST, cmdsId));
                                    testIds.remove(globalId);
                                    cmdstestIds.remove();
                                }
                            }
                            detail.cmdsIds.addAll(cmdsTestIds);
                            detail.ids.addAll(testIds);
                            flag = true;
                            break;
                        }
                    }
                    if(!flag){
                        EntityDetails detail = new EntityDetails();
                        detail.type = EntityType.TEST.name();
                        detail.cmdsIds = cmdsTestIds;
                        detail.ids = testIds;
                        details.add(detail);
                    }
                }
                if(!cmdsVideoIds.isEmpty() && !videoIds.isEmpty()){
                    boolean flag = false;
                    for(EntityDetails detail : details){
                        if(detail.type.equals(EntityType.VIDEO.name())){
                            Iterator<String> cmdsvideoIds = cmdsVideoIds.iterator();
                            while(cmdsvideoIds.hasNext()){
                                String cmdsId = cmdsvideoIds.next();
                                if(detail.cmdsIds.contains(cmdsId)){
                                    String globalId = getGlobalId(new SrcEntity(EntityType.CMDSVIDEO, cmdsId));
                                    videoIds.remove(globalId);
                                    cmdsvideoIds.remove();
                                }
                            }
                            detail.cmdsIds.addAll(cmdsVideoIds);
                            detail.ids.addAll(videoIds);
                            flag = true;
                            break;
                        }
                    }
                    if(!flag){
                        EntityDetails detail = new EntityDetails();
                        detail.type = EntityType.VIDEO.name();
                        detail.cmdsIds = cmdsVideoIds;
                        detail.ids = videoIds;
                        details.add(detail);
                    }
                }
                if(!cmdsModuleIds.isEmpty() && !moduleIds.isEmpty()){
                    boolean flag = false;
                    for(EntityDetails detail : details){
                        if(detail.type.equals(EntityType.MODULE.name())){
                            Iterator<String> cmdsmoduleIds = cmdsModuleIds.iterator();
                            while(cmdsmoduleIds.hasNext()){
                                String cmdsId = cmdsmoduleIds.next();
                                if(detail.cmdsIds.contains(cmdsId)){
                                    String globalId = getGlobalId(new SrcEntity(EntityType.CMDSMODULE, cmdsId));
                                    moduleIds.remove(globalId);
                                    cmdsmoduleIds.remove();
                                }
                            }
                            detail.cmdsIds.addAll(cmdsModuleIds);
                            detail.ids.addAll(moduleIds);
                            flag = true;
                            break;
                        }
                    }
                    if(!flag){
                        EntityDetails detail = new EntityDetails();
                        detail.type = EntityType.MODULE.name();
                        detail.cmdsIds = cmdsModuleIds;
                        detail.ids = moduleIds;
                        details.add(detail);
                    }
                }
                if(!cmdsAssignmentIds.isEmpty() && !assignmentIds.isEmpty()){
                    boolean flag = false;
                    for(EntityDetails detail : details){
                        if(detail.type.equals(EntityType.ASSIGNMENT.name())){
                            Iterator<String> cmdsassignmentIds = cmdsAssignmentIds.iterator();
                            while(cmdsassignmentIds.hasNext()){
                                String cmdsId = cmdsassignmentIds.next();
                                if(detail.cmdsIds.contains(cmdsId)){
                                    String globalId = getGlobalId(new SrcEntity(EntityType.CMDSASSIGNMENT, cmdsId));
                                    assignmentIds.remove(globalId);
                                    cmdsassignmentIds.remove();
                                }
                            }
                            detail.cmdsIds.addAll(cmdsAssignmentIds);
                            detail.ids.addAll(assignmentIds);
                            flag = true;
                            break;
                        }
                    }
                    if(!flag){
                        EntityDetails detail = new EntityDetails();
                        detail.type = EntityType.ASSIGNMENT.name();
                        detail.cmdsIds = cmdsAssignmentIds;
                        detail.ids = assignmentIds;
                        details.add(detail);
                    }
                }
                if(!cmdsDocumentIds.isEmpty() && !documentIds.isEmpty()){
                    boolean flag = false;
                    for(EntityDetails detail : details){
                        if(detail.type.equals(EntityType.DOCUMENT.name())){
                            Iterator<String> cmdsdocumentIds = cmdsDocumentIds.iterator();
                            while(cmdsdocumentIds.hasNext()){
                                String cmdsId = cmdsdocumentIds.next();
                                if(detail.cmdsIds.contains(cmdsId)){
                                    String globalId = getGlobalId(new SrcEntity(EntityType.CMDSDOCUMENT, cmdsId));
                                    documentIds.remove(globalId);
                                    cmdsdocumentIds.remove();
                                }
                            }
                            detail.cmdsIds.addAll(cmdsDocumentIds);
                            detail.ids.addAll(documentIds);
                            flag = true;
                            break;
                        }
                    }
                    if(!flag){
                        EntityDetails detail = new EntityDetails();
                        detail.type = EntityType.DOCUMENT.name();
                        detail.cmdsIds = cmdsDocumentIds;
                        detail.ids = documentIds;
                        details.add(detail);
                    }
                }
                subMetaData.details = details;
                for(SubjectMetadata singleMetaData : schedule.metadata){
                    if(addScheduleReq.boardId.equals(singleMetaData.id)){
                        singleMetaData = subMetaData;
                        break;
                    }
                }
                scheduleRepo.save(schedule);
                response.saved = true;
            }else{
                schedule.boardIds.add(addScheduleReq.boardId);
                SubjectMetadata subMetaData = new SubjectMetadata();
                subMetaData.id = addScheduleReq.boardId;
                subMetaData.name = addScheduleReq.boardName;
                List<EntityDetails> details = new ArrayList<EntityDetails>();
                if(!cmdsTestIds.isEmpty() && !testIds.isEmpty()){
                    EntityDetails detail = new EntityDetails();
                    detail.type = EntityType.TEST.name();
                    detail.cmdsIds = cmdsTestIds;
                    detail.ids = testIds;
                    details.add(detail);
                }
                if(!cmdsVideoIds.isEmpty() && !videoIds.isEmpty()){
                    EntityDetails detail = new EntityDetails();
                    detail.type = EntityType.VIDEO.name();
                    detail.cmdsIds = cmdsVideoIds;
                    detail.ids = videoIds;
                    details.add(detail);
                }
                if(!cmdsModuleIds.isEmpty() && !moduleIds.isEmpty()){
                    EntityDetails detail = new EntityDetails();
                    detail.type = EntityType.MODULE.name();
                    detail.cmdsIds = cmdsModuleIds;
                    detail.ids = moduleIds;
                    details.add(detail);
                }
                if(!cmdsAssignmentIds.isEmpty() && !assignmentIds.isEmpty()){
                    EntityDetails detail = new EntityDetails();
                    detail.type = EntityType.ASSIGNMENT.name();
                    detail.cmdsIds = cmdsAssignmentIds;
                    detail.ids = assignmentIds;
                    details.add(detail);
                }
                if(!cmdsDocumentIds.isEmpty() && !documentIds.isEmpty()){
                    EntityDetails detail = new EntityDetails();
                    detail.type = EntityType.DOCUMENT.name();
                    detail.cmdsIds = cmdsDocumentIds;
                    detail.ids = documentIds;
                    details.add(detail);
                }
                subMetaData.details = details;
                schedule.metadata.add(subMetaData);
                scheduleRepo.save(schedule);
                response.saved = true;
            }
        }else{
            schedule = new Schedule();
            schedule.orgId = addScheduleReq.orgId;
            schedule.programId = addScheduleReq.programId;
            schedule.centerId = addScheduleReq.centerId;
            schedule.sectionId = addScheduleReq.sectionId;
            schedule.month = addScheduleReq.month;
            schedule.day = addScheduleReq.day;
            schedule.boardIds.add(addScheduleReq.boardId);
            SubjectMetadata subMetaData = new SubjectMetadata();
            subMetaData.id = addScheduleReq.boardId;
            subMetaData.name = addScheduleReq.boardName;
            List<EntityDetails> details = new ArrayList<EntityDetails>();
            if(!cmdsTestIds.isEmpty() && !testIds.isEmpty()){
                EntityDetails detail = new EntityDetails();
                detail.type = EntityType.TEST.name();
                detail.cmdsIds = cmdsTestIds;
                detail.ids = testIds;
                details.add(detail);
            }
            if(!cmdsVideoIds.isEmpty() && !videoIds.isEmpty()){
                EntityDetails detail = new EntityDetails();
                detail.type = EntityType.VIDEO.name();
                detail.cmdsIds = cmdsVideoIds;
                detail.ids = videoIds;
                details.add(detail);
            }
            if(!cmdsModuleIds.isEmpty() && !moduleIds.isEmpty()){
                EntityDetails detail = new EntityDetails();
                detail.type = EntityType.MODULE.name();
                detail.cmdsIds = cmdsModuleIds;
                detail.ids = moduleIds;
                details.add(detail);
            }
            if(!cmdsAssignmentIds.isEmpty() && !assignmentIds.isEmpty()){
                EntityDetails detail = new EntityDetails();
                detail.type = EntityType.ASSIGNMENT.name();
                detail.cmdsIds = cmdsAssignmentIds;
                detail.ids = assignmentIds;
                details.add(detail);
            }
            if(!cmdsDocumentIds.isEmpty() && !documentIds.isEmpty()){
                EntityDetails detail = new EntityDetails();
                detail.type = EntityType.DOCUMENT.name();
                detail.cmdsIds = cmdsDocumentIds;
                detail.ids = documentIds;
                details.add(detail);
            }
            subMetaData.details = details;
            schedule.metadata.add(subMetaData);
            scheduleRepo.save(schedule);
            response.saved = true;
        }
        return response;
    }

}

