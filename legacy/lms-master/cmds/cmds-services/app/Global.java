import play.Application;

import com.vedantu.VedantuGlobalSettings;
import com.vedantu.cmds.content.exporters.DocumentExporter;
import com.vedantu.cmds.content.exporters.FileExporter;
import com.vedantu.cmds.content.exporters.QuestionExporter;
import com.vedantu.cmds.content.exporters.TestExporter;
import com.vedantu.cmds.content.exporters.VideoExporter;
import com.vedantu.cmds.daos.CMDSAssignmentDAO;
import com.vedantu.cmds.daos.CMDSDocumentDAO;
import com.vedantu.cmds.daos.CMDSFileDAO;
import com.vedantu.cmds.daos.CMDSFolderDAO;
import com.vedantu.cmds.daos.CMDSModuleDAO;
import com.vedantu.cmds.daos.CMDSQuestionDAO;
import com.vedantu.cmds.daos.CMDSQuestionSetDAO;
import com.vedantu.cmds.daos.CMDSTestDAO;
import com.vedantu.cmds.daos.CMDSVideoDAO;
import com.vedantu.cmds.daos.SDCardDAO;
import com.vedantu.cmds.daos.SDCardGroupDAO;
import com.vedantu.cmds.factory.ContentExporterFactory;
import com.vedantu.cmds.mgmt.publishers.AssignmentPublisher;
import com.vedantu.cmds.mgmt.publishers.DocumentPublisher;
import com.vedantu.cmds.mgmt.publishers.EntityTypePublisherFactory;
import com.vedantu.cmds.mgmt.publishers.FilePublisher;
import com.vedantu.cmds.mgmt.publishers.ModulePublisher;
import com.vedantu.cmds.mgmt.publishers.QuestionPublisher;
import com.vedantu.cmds.mgmt.publishers.TestPublisher;
import com.vedantu.cmds.mgmt.publishers.VideoPublisher;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.events.apis.EntityIndexEventMapper;
import com.vedantu.commons.events.apis.EventRegistrar;
import com.vedantu.content.daos.AssignmentDAO;
import com.vedantu.content.daos.CommentDAO;
import com.vedantu.content.daos.DiscussionDAO;
import com.vedantu.content.daos.DocumentDAO;
import com.vedantu.content.daos.FileDAO;
import com.vedantu.content.daos.ModuleDAO;
import com.vedantu.content.daos.QuestionDAO;
import com.vedantu.content.daos.TestDAO;
import com.vedantu.content.daos.VideoDAO;
import com.vedantu.content.daos.challenges.ChallengeDAO;
import com.vedantu.organization.daos.LicensingPlanDAO;
import com.vedantu.organization.daos.OrgCenterDAO;
import com.vedantu.organization.daos.OrgProgramDAO;
import com.vedantu.organization.daos.OrgSectionDAO;
import com.vedantu.organization.daos.OrganizationDAO;

public class Global extends VedantuGlobalSettings {

    @Override
    public void onStart(Application app) {

        entityDAOFactoryRegister();
        eventDetailsRegister();
        registerEntityPublisher();
        registerEntityIndexEventMapperRegister();
        registerContentExporter();
    }

    private void entityDAOFactoryRegister() {

        // DAO REGISTRATIONS
        EntityTypeDAOFactory.INSTANCE.register(EntityType.COMMENT, CommentDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.DISCUSSION, DiscussionDAO.INSTANCE);

        EntityTypeDAOFactory.INSTANCE.register(EntityType.QUESTION, QuestionDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.CHALLENGE, ChallengeDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.CMDSQUESTION, CMDSQuestionDAO.INSTANCE);

        EntityTypeDAOFactory.INSTANCE.register(EntityType.TEST, TestDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.MODULE, ModuleDAO.INSTANCE);

        EntityTypeDAOFactory.INSTANCE.register(EntityType.CMDSTEST, CMDSTestDAO.INSTANCE);

        EntityTypeDAOFactory.INSTANCE.register(EntityType.ASSIGNMENT, AssignmentDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.CMDSASSIGNMENT,
                CMDSAssignmentDAO.INSTANCE);

        EntityTypeDAOFactory.INSTANCE.register(EntityType.FOLDER, CMDSFolderDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.CMDSQUESTIONSET,
                CMDSQuestionSetDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.CMDSVIDEO, CMDSVideoDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.CMDSMODULE, CMDSModuleDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.CMDSDOCUMENT, CMDSDocumentDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.CMDSFILE, CMDSFileDAO.INSTANCE);

        EntityTypeDAOFactory.INSTANCE.register(EntityType.ORGANIZATION, OrganizationDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.PROGRAM, OrgProgramDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.CENTER, OrgCenterDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.SECTION, OrgSectionDAO.INSTANCE);

        EntityTypeDAOFactory.INSTANCE.register(EntityType.VIDEO, VideoDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.DOCUMENT, DocumentDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.FILE, FileDAO.INSTANCE);

        // Non contents
        EntityTypeDAOFactory.INSTANCE.register(EntityType.PLAN, LicensingPlanDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.SDCARD, SDCardDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.SDCARDGROUP, SDCardGroupDAO.INSTANCE);
    }

    private void eventDetailsRegister() {

        // EVENT DETAILS REGISTRATIONS
        EventRegistrar.INSTANCE.add(EventType.INDEX_CMDS_QUESTION);

        EventRegistrar.INSTANCE.add(EventType.INDEX_TEST);
        EventRegistrar.INSTANCE.add(EventType.END_TEST);
        EventRegistrar.INSTANCE.add(EventType.INDEX_CMDS_TEST);
        EventRegistrar.INSTANCE.add(EventType.INDEX_ASSIGNMENT);
        EventRegistrar.INSTANCE.add(EventType.INDEX_CMDS_ASSIGNMENT);
        EventRegistrar.INSTANCE.add(EventType.INDEX_CMDS_DOCUMENT);
        EventRegistrar.INSTANCE.add(EventType.INDEX_DOCUMENT);
        EventRegistrar.INSTANCE.add(EventType.CONVERT_DOCUMENT);
        EventRegistrar.INSTANCE.add(EventType.INDEX_MODULE);

        EventRegistrar.INSTANCE.add(EventType.INDEX_QUESTION);
        EventRegistrar.INSTANCE.add(EventType.END_CHALLENGE);
        EventRegistrar.INSTANCE.add(EventType.INDEX_CHALLENGE);
        EventRegistrar.INSTANCE.add(EventType.ADD_SOLUTION);
        EventRegistrar.INSTANCE.add(EventType.PUBLISH_ENTITY);
        EventRegistrar.INSTANCE.add(EventType.UPLOAD_TEST_RESULT);
        EventRegistrar.INSTANCE.add(EventType.SEND_EMAIL);
        EventRegistrar.INSTANCE.add(EventType.INDEX_CMDS_VIDEO);
        EventRegistrar.INSTANCE.add(EventType.INDEX_CMDS_MODULE);
        EventRegistrar.INSTANCE.add(EventType.INDEX_VIDEO);
        EventRegistrar.INSTANCE.add(EventType.REINDEX_CMDS_RESOURCE);
        EventRegistrar.INSTANCE.add(EventType.CONVERT_VIDEO);
        EventRegistrar.INSTANCE.add(EventType.UPLOAD_VIDEO);

        EventRegistrar.INSTANCE.add(EventType.INDEX_CMDS_FILE);

        EventRegistrar.INSTANCE.add(EventType.INDEX_FILE);
        EventRegistrar.INSTANCE.add(EventType.PROCESS_FILE);
        EventRegistrar.INSTANCE.add(EventType.EXPORT);
        EventRegistrar.INSTANCE.add(EventType.MADE_VISIBLE);
        EventRegistrar.INSTANCE.add(EventType.CALCULATE_SIZE);
        EventRegistrar.INSTANCE.add(EventType.SD_CARD_SPLIT);
    }

    private void registerEntityPublisher() {

        // PUBLISHING REGISTRATIONS
        EntityTypePublisherFactory.INSTANCE.register(EntityType.CMDSQUESTION,
                QuestionPublisher.INSTANCE);
        EntityTypePublisherFactory.INSTANCE.register(EntityType.CMDSASSIGNMENT,
                AssignmentPublisher.INSTANCE);
        EntityTypePublisherFactory.INSTANCE.register(EntityType.CMDSTEST, TestPublisher.INSTANCE);
        EntityTypePublisherFactory.INSTANCE.register(EntityType.CMDSVIDEO, VideoPublisher.INSTANCE);
        EntityTypePublisherFactory.INSTANCE.register(EntityType.CMDSMODULE,
                ModulePublisher.INSTANCE);
        EntityTypePublisherFactory.INSTANCE.register(EntityType.CMDSFILE, FilePublisher.INSTANCE);
        EntityTypePublisherFactory.INSTANCE.register(EntityType.CMDSDOCUMENT,
                DocumentPublisher.INSTANCE);

    }

    private void registerEntityIndexEventMapperRegister() {

        // INDEXING EVENT REGISTRATIONS
        EntityIndexEventMapper.INSTANCE.put(EntityType.CMDSQUESTION, EventType.INDEX_CMDS_QUESTION);
        EntityIndexEventMapper.INSTANCE.put(EntityType.CMDSVIDEO, EventType.INDEX_CMDS_VIDEO);
        EntityIndexEventMapper.INSTANCE.put(EntityType.CMDSMODULE, EventType.INDEX_CMDS_MODULE);
        EntityIndexEventMapper.INSTANCE.put(EntityType.CMDSFILE, EventType.INDEX_CMDS_FILE);
        EntityIndexEventMapper.INSTANCE.put(EntityType.CMDSTEST, EventType.INDEX_CMDS_TEST);
        EntityIndexEventMapper.INSTANCE.put(EntityType.CMDSASSIGNMENT,
                EventType.INDEX_CMDS_ASSIGNMENT);
        EntityIndexEventMapper.INSTANCE.put(EntityType.CMDSDOCUMENT, EventType.INDEX_CMDS_DOCUMENT);

        EntityIndexEventMapper.INSTANCE.put(EntityType.VIDEO, EventType.INDEX_VIDEO);
        EntityIndexEventMapper.INSTANCE.put(EntityType.MODULE, EventType.INDEX_MODULE);
        EntityIndexEventMapper.INSTANCE.put(EntityType.FILE, EventType.INDEX_FILE);
        EntityIndexEventMapper.INSTANCE.put(EntityType.DOCUMENT, EventType.INDEX_DOCUMENT);
        EntityIndexEventMapper.INSTANCE.put(EntityType.TEST, EventType.INDEX_TEST);
        EntityIndexEventMapper.INSTANCE.put(EntityType.ASSIGNMENT, EventType.INDEX_ASSIGNMENT);
        EntityIndexEventMapper.INSTANCE.put(EntityType.QUESTION, EventType.INDEX_QUESTION);
        EntityIndexEventMapper.INSTANCE.put(EntityType.DISCUSSION, EventType.INDEX_DISCUSSION);

    }

    private void registerContentExporter() {

        // INDEXING EVENT REGISTRATIONS
        ContentExporterFactory.INSTANCE
                .register(EntityType.CMDSQUESTION, QuestionExporter.INSTANCE);

        ContentExporterFactory.INSTANCE.register(EntityType.QUESTION, QuestionExporter.INSTANCE);

        ContentExporterFactory.INSTANCE.register(EntityType.CMDSTEST, TestExporter.INSTANCE);

        ContentExporterFactory.INSTANCE.register(EntityType.CMDSVIDEO, VideoExporter.INSTANCE);
        // //TODO
        // ///ContentExporterFactory.INSTANCE.register(EntityType.CMDSMODULE,
        // VideoExporter.INSTANCE);
        ContentExporterFactory.INSTANCE
                .register(EntityType.CMDSDOCUMENT, DocumentExporter.INSTANCE);

        ContentExporterFactory.INSTANCE.register(EntityType.CMDSFILE, FileExporter.INSTANCE);

    }
}
