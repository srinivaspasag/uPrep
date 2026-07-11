import play.Application;

import com.vedantu.VedantuGlobalSettings;
import com.vedantu.cmds.content.exporters.AssignmentExporter;
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
import com.vedantu.cmds.factory.ContentExporterFactory;
import com.vedantu.cmds.managers.CMDSAssignmentManager;
import com.vedantu.cmds.managers.CMDSDocumentManager;
import com.vedantu.cmds.managers.CMDSFileManager;
import com.vedantu.cmds.managers.CMDSModuleManager;
import com.vedantu.cmds.managers.CMDSQuestionManager;
import com.vedantu.cmds.managers.CMDSTestManager;
import com.vedantu.cmds.managers.CMDSVideoManager;
import com.vedantu.cmds.mgmt.publishers.AssignmentPublisher;
import com.vedantu.cmds.mgmt.publishers.DocumentPublisher;
import com.vedantu.cmds.mgmt.publishers.EntityTypePublisherFactory;
import com.vedantu.cmds.mgmt.publishers.FilePublisher;
import com.vedantu.cmds.mgmt.publishers.ModulePublisher;
import com.vedantu.cmds.mgmt.publishers.QuestionPublisher;
import com.vedantu.cmds.mgmt.publishers.TestPublisher;
import com.vedantu.cmds.mgmt.publishers.VideoPublisher;
import com.vedantu.comm.daos.RemarkDAO;
import com.vedantu.comm.managers.news.NewsFeedSecurityVaildator;
import com.vedantu.comm.managers.news.populators.AssignmentNewsPopulator;
import com.vedantu.comm.managers.news.populators.CenterNewsPopulator;
import com.vedantu.comm.managers.news.populators.ChallengeNewsPopulator;
import com.vedantu.comm.managers.news.populators.CommentNewsPopulator;
import com.vedantu.comm.managers.news.populators.DiscussionNewsPopulator;
import com.vedantu.comm.managers.news.populators.DocumenNewsPopulator;
import com.vedantu.comm.managers.news.populators.EntityDetailsPopulatorFactory;
import com.vedantu.comm.managers.news.populators.EntityNewsDetailsFactory;
import com.vedantu.comm.managers.news.populators.FileNewsPopulator;
import com.vedantu.comm.managers.news.populators.ModuleNewsPopulator;
import com.vedantu.comm.managers.news.populators.ProgramNewsPopulator;
import com.vedantu.comm.managers.news.populators.QuestionNewsPopulator;
import com.vedantu.comm.managers.news.populators.RemarksNewsPopulator;
import com.vedantu.comm.managers.news.populators.SectionNewsPopulator;
import com.vedantu.comm.managers.news.populators.SolutionNewsPopulator;
import com.vedantu.comm.managers.news.populators.StatusFeedPopulator;
import com.vedantu.comm.managers.news.populators.TestNewsPopulator;
import com.vedantu.comm.managers.news.populators.UserNewsPopulator;
import com.vedantu.comm.managers.news.populators.VideoNewsPopulator;
import com.vedantu.comm.news.details.ChallengeNewsDetails;
import com.vedantu.comm.news.details.CommentNewsEntityDetails;
import com.vedantu.comm.news.details.DiscussionNewsEntityDetails;
import com.vedantu.comm.news.details.DocumentNewsEntityDetails;
import com.vedantu.comm.news.details.FileNewsEntityDetails;
import com.vedantu.comm.news.details.ModuleNewsEntityDetails;
import com.vedantu.comm.news.details.QuestionNewsEntityDetails;
import com.vedantu.comm.news.details.RemarkNewsEntityDetails;
import com.vedantu.comm.news.details.SolutionNewsDetails;
import com.vedantu.comm.news.details.StatusFeedNewsEntityDetails;
import com.vedantu.comm.news.details.TestNewsEntityDetails;
import com.vedantu.comm.news.details.VideoNewsEntityDetails;
import com.vedantu.commons.entity.factory.EntityTypeContentManagerFactory;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.events.apis.EntityIndexEventMapper;
import com.vedantu.content.daos.AssignmentDAO;
import com.vedantu.content.daos.CommentDAO;
import com.vedantu.content.daos.DiscussionDAO;
import com.vedantu.content.daos.DocumentDAO;
import com.vedantu.content.daos.FileDAO;
import com.vedantu.content.daos.ModuleDAO;
import com.vedantu.content.daos.QuestionDAO;
import com.vedantu.content.daos.SolutionDAO;
import com.vedantu.content.daos.StatusFeedDAO;
import com.vedantu.content.daos.TestDAO;
import com.vedantu.content.daos.VideoDAO;
import com.vedantu.content.daos.challenges.ChallengeDAO;
import com.vedantu.content.managers.AssignmentManager;
import com.vedantu.content.managers.DocumentManager;
import com.vedantu.content.managers.FileManager;
import com.vedantu.content.managers.ModuleManager;
import com.vedantu.content.managers.QuestionManager;
import com.vedantu.content.managers.TestManager;
import com.vedantu.content.managers.VideoManager;
import com.vedantu.eventbus.utils.ProcessorFactory;
import com.vedantu.organization.daos.OrgCenterDAO;
import com.vedantu.organization.daos.OrgProgramDAO;
import com.vedantu.organization.daos.OrgSectionDAO;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.search.es.ElasticSearchManager;

public class Global extends VedantuGlobalSettings {

    @Override
    public void beforeStart(Application app) {

        super.beforeStart(app);
        ElasticSearchManager.getInstance();
        ProcessorFactory.getInstance();
        entityDAOFactoryRegister();
        entityEventIndexMapperRegister();
        entityPublisherRegister();
        eventDetailPopulatorRegister();
        entityNewsDetailRegister();
        registerContentExporter();
        registerPublicEvents();
        registerContentManagers();
    }

    private void registerContentManagers() {

        EntityTypeContentManagerFactory.INSTANCE.register(EntityType.VIDEO, VideoManager.class);
        EntityTypeContentManagerFactory.INSTANCE.register(EntityType.FILE, FileManager.class);
        EntityTypeContentManagerFactory.INSTANCE.register(EntityType.DOCUMENT,
                DocumentManager.class);
        EntityTypeContentManagerFactory.INSTANCE.register(EntityType.QUESTION,
                QuestionManager.class);
        EntityTypeContentManagerFactory.INSTANCE.register(EntityType.TEST, TestManager.class);
        EntityTypeContentManagerFactory.INSTANCE.register(EntityType.MODULE, ModuleManager.class);
        EntityTypeContentManagerFactory.INSTANCE.register(EntityType.ASSIGNMENT, AssignmentManager.class);

        EntityTypeContentManagerFactory.INSTANCE.register(EntityType.CMDSVIDEO,
                CMDSVideoManager.class);
        EntityTypeContentManagerFactory.INSTANCE.register(EntityType.CMDSDOCUMENT,
                CMDSDocumentManager.class);
        EntityTypeContentManagerFactory.INSTANCE.register(EntityType.CMDSFILE,
                CMDSFileManager.class);
        EntityTypeContentManagerFactory.INSTANCE.register(EntityType.CMDSQUESTION,
                CMDSQuestionManager.class);
        EntityTypeContentManagerFactory.INSTANCE.register(EntityType.CMDSTEST,
                CMDSTestManager.class);
        EntityTypeContentManagerFactory.INSTANCE.register(EntityType.CMDSMODULE,
                CMDSModuleManager.class);
        EntityTypeContentManagerFactory.INSTANCE.register(EntityType.CMDSASSIGNMENT,
                CMDSAssignmentManager.class);

    }

    private void entityNewsDetailRegister() {

        // details registration for news

        EntityNewsDetailsFactory.INSTANCE.register(EntityType.VIDEO, VideoNewsEntityDetails.class);
        EntityNewsDetailsFactory.INSTANCE.register(EntityType.QUESTION,
                QuestionNewsEntityDetails.class);
        EntityNewsDetailsFactory.INSTANCE.register(EntityType.SOLUTION, SolutionNewsDetails.class);
        EntityNewsDetailsFactory.INSTANCE.register(EntityType.DISCUSSION,
                DiscussionNewsEntityDetails.class);
        EntityNewsDetailsFactory.INSTANCE
                .register(EntityType.CHALLENGE, ChallengeNewsDetails.class);

        EntityNewsDetailsFactory.INSTANCE.register(EntityType.TEST, TestNewsEntityDetails.class);
        EntityNewsDetailsFactory.INSTANCE.register(EntityType.ASSIGNMENT,
                TestNewsEntityDetails.class);
        EntityNewsDetailsFactory.INSTANCE.register(EntityType.STATUSFEED,
                StatusFeedNewsEntityDetails.class);
        EntityNewsDetailsFactory.INSTANCE.register(EntityType.COMMENT,
                CommentNewsEntityDetails.class);

        EntityNewsDetailsFactory.INSTANCE.register(EntityType.FILE, FileNewsEntityDetails.class);

        EntityNewsDetailsFactory.INSTANCE.register(EntityType.DOCUMENT,
                DocumentNewsEntityDetails.class);

        EntityNewsDetailsFactory.INSTANCE
                .register(EntityType.MODULE, ModuleNewsEntityDetails.class);

        EntityNewsDetailsFactory.INSTANCE
                .register(EntityType.REMARK, RemarkNewsEntityDetails.class);

        // TODO add document and file specific details...
    }

    private void eventDetailPopulatorRegister() {

        // Populator registrations for news
        EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.VIDEO,
                VideoNewsPopulator.INSTANCE);

        EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.MODULE,
                ModuleNewsPopulator.INSTANCE);

        EntityDetailsPopulatorFactory.INSTANCE
                .register(EntityType.FILE, FileNewsPopulator.INSTANCE);

        EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.DOCUMENT,
                DocumenNewsPopulator.INSTANCE);

        EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.STATUSFEED,
                StatusFeedPopulator.INSTANCE);
        EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.DISCUSSION,
                DiscussionNewsPopulator.INSTANCE);
        EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.QUESTION,
                QuestionNewsPopulator.INSTANCE);
        EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.SOLUTION,
                SolutionNewsPopulator.INSTANCE);
        EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.CHALLENGE,
                ChallengeNewsPopulator.INSTANCE);
        EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.COMMENT,
                CommentNewsPopulator.INSTANCE);
        EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.REMARK,
                RemarksNewsPopulator.INSTANCE);
        EntityDetailsPopulatorFactory.INSTANCE
                .register(EntityType.TEST, TestNewsPopulator.INSTANCE);
        EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.ASSIGNMENT,
                AssignmentNewsPopulator.INSTANCE);

        EntityDetailsPopulatorFactory.INSTANCE
                .register(EntityType.USER, UserNewsPopulator.INSTANCE);
        EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.PROGRAM,
                ProgramNewsPopulator.INSTANCE);
        EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.CENTER,
                CenterNewsPopulator.INSTANCE);
        EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.SECTION,
                SectionNewsPopulator.INSTANCE);

        // TODO add document and file specific details...
    }

    private void entityPublisherRegister() {

        EntityTypePublisherFactory.INSTANCE.register(EntityType.CMDSQUESTION,
                QuestionPublisher.INSTANCE);
        EntityTypePublisherFactory.INSTANCE.register(EntityType.CMDSTEST, TestPublisher.INSTANCE);
        EntityTypePublisherFactory.INSTANCE.register(EntityType.CMDSASSIGNMENT,
                AssignmentPublisher.INSTANCE);
        EntityTypePublisherFactory.INSTANCE.register(EntityType.CMDSVIDEO, VideoPublisher.INSTANCE);
        EntityTypePublisherFactory.INSTANCE.register(EntityType.CMDSDOCUMENT,
                DocumentPublisher.INSTANCE);
        EntityTypePublisherFactory.INSTANCE.register(EntityType.CMDSFILE, FilePublisher.INSTANCE);
        EntityTypePublisherFactory.INSTANCE.register(EntityType.CMDSMODULE,
                ModulePublisher.INSTANCE);

    }

    private void entityEventIndexMapperRegister() {

        EntityIndexEventMapper.INSTANCE.put(EntityType.CMDSQUESTION, EventType.INDEX_CMDS_QUESTION);
        EntityIndexEventMapper.INSTANCE.put(EntityType.CMDSVIDEO, EventType.INDEX_CMDS_VIDEO);
        EntityIndexEventMapper.INSTANCE.put(EntityType.CMDSMODULE, EventType.INDEX_CMDS_MODULE);
        EntityIndexEventMapper.INSTANCE.put(EntityType.CMDSASSIGNMENT,
                EventType.INDEX_CMDS_ASSIGNMENT);
        EntityIndexEventMapper.INSTANCE.put(EntityType.CMDSDOCUMENT, EventType.INDEX_CMDS_DOCUMENT);
        EntityIndexEventMapper.INSTANCE.put(EntityType.CMDSFILE, EventType.INDEX_CMDS_FILE);

        EntityIndexEventMapper.INSTANCE.put(EntityType.VIDEO, EventType.INDEX_VIDEO);
        EntityIndexEventMapper.INSTANCE.put(EntityType.DOCUMENT, EventType.INDEX_DOCUMENT);
        EntityIndexEventMapper.INSTANCE.put(EntityType.TEST, EventType.INDEX_TEST);
        EntityIndexEventMapper.INSTANCE.put(EntityType.ASSIGNMENT, EventType.INDEX_ASSIGNMENT);
        EntityIndexEventMapper.INSTANCE.put(EntityType.QUESTION, EventType.INDEX_QUESTION);
        EntityIndexEventMapper.INSTANCE.put(EntityType.DISCUSSION, EventType.INDEX_DISCUSSION);
        EntityIndexEventMapper.INSTANCE.put(EntityType.FILE, EventType.INDEX_FILE);
        EntityIndexEventMapper.INSTANCE.put(EntityType.MODULE, EventType.INDEX_MODULE);

    }

    private void entityDAOFactoryRegister() {

        // DAO REGISTRATIONS
        EntityTypeDAOFactory.INSTANCE.register(EntityType.STATUSFEED, StatusFeedDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.QUESTION, QuestionDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.SOLUTION, SolutionDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.CHALLENGE, ChallengeDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.CMDSQUESTION, CMDSQuestionDAO.INSTANCE);

        EntityTypeDAOFactory.INSTANCE.register(EntityType.COMMENT, CommentDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.DISCUSSION, DiscussionDAO.INSTANCE);

        EntityTypeDAOFactory.INSTANCE.register(EntityType.TEST, TestDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.CMDSTEST, CMDSTestDAO.INSTANCE);

        EntityTypeDAOFactory.INSTANCE.register(EntityType.ASSIGNMENT, AssignmentDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.CMDSASSIGNMENT,
                CMDSAssignmentDAO.INSTANCE);

        EntityTypeDAOFactory.INSTANCE.register(EntityType.CMDSVIDEO, CMDSVideoDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.VIDEO, VideoDAO.INSTANCE);

        EntityTypeDAOFactory.INSTANCE.register(EntityType.CMDSMODULE, CMDSModuleDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.MODULE, ModuleDAO.INSTANCE);

        EntityTypeDAOFactory.INSTANCE.register(EntityType.CMDSDOCUMENT, CMDSDocumentDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.DOCUMENT, DocumentDAO.INSTANCE);

        EntityTypeDAOFactory.INSTANCE.register(EntityType.CMDSFILE, CMDSFileDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.FILE, FileDAO.INSTANCE);

        EntityTypeDAOFactory.INSTANCE.register(EntityType.FOLDER, CMDSFolderDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.CMDSQUESTIONSET,
                CMDSQuestionSetDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.REMARK, RemarkDAO.INSTANCE);

        EntityTypeDAOFactory.INSTANCE.register(EntityType.PROGRAM, OrgProgramDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.CENTER, OrgCenterDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.SECTION, OrgSectionDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.ORGANIZATION,OrganizationDAO.INSTANCE);

    }

    private void registerContentExporter() {

        // INDEXING EVENT REGISTRATIONS
        ContentExporterFactory.INSTANCE
                .register(EntityType.CMDSQUESTION, QuestionExporter.INSTANCE);

        ContentExporterFactory.INSTANCE.register(EntityType.QUESTION, QuestionExporter.INSTANCE);

        ContentExporterFactory.INSTANCE.register(EntityType.CMDSTEST, TestExporter.INSTANCE);
        ContentExporterFactory.INSTANCE.register(EntityType.CMDSASSIGNMENT,
                AssignmentExporter.INSTANCE);

        ContentExporterFactory.INSTANCE.register(EntityType.CMDSVIDEO, VideoExporter.INSTANCE);
        ContentExporterFactory.INSTANCE
                .register(EntityType.CMDSDOCUMENT, DocumentExporter.INSTANCE);

        ContentExporterFactory.INSTANCE.register(EntityType.CMDSFILE, FileExporter.INSTANCE);

    }

    private void registerPublicEvents() {

        NewsFeedSecurityVaildator.addPublicEvent(EventType.ADD_COMMENT);
        NewsFeedSecurityVaildator.addPublicEvent(EventType.ADD_SOLUTION);
        NewsFeedSecurityVaildator.addPublicEvent(EventType.ATTEMPT_ENTITY);
        NewsFeedSecurityVaildator.addPublicEvent(EventType.FOLLOW_ENTITY);
        NewsFeedSecurityVaildator.addPublicEvent(EventType.VOTE_ENTITY);
        NewsFeedSecurityVaildator.addPublicEvent(EventType.INDEX_CHALLENGE);
        NewsFeedSecurityVaildator.addPublicEvent(EventType.END_CHALLENGE);

    }

}
