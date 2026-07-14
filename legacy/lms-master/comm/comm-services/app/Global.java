import play.Application;

import com.vedantu.VedantuGlobalSettings;
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
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.events.apis.EventRegistrar;
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
import com.vedantu.organization.daos.OrgCenterDAO;
import com.vedantu.organization.daos.OrgProgramDAO;
import com.vedantu.organization.daos.OrgSectionDAO;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.user.daos.UserDAO;

public class Global extends VedantuGlobalSettings {

	@Override
	public void onStart(Application app) {

		EntityTypeDAOFactory.INSTANCE.register(EntityType.STATUSFEED,
				StatusFeedDAO.INSTANCE);
		EntityTypeDAOFactory.INSTANCE.register(EntityType.QUESTION,
				QuestionDAO.INSTANCE);
		EntityTypeDAOFactory.INSTANCE.register(EntityType.SOLUTION,
				SolutionDAO.INSTANCE);
		EntityTypeDAOFactory.INSTANCE.register(EntityType.DISCUSSION,
				DiscussionDAO.INSTANCE);
		EntityTypeDAOFactory.INSTANCE.register(EntityType.TEST,
				TestDAO.INSTANCE);
		EntityTypeDAOFactory.INSTANCE.register(EntityType.ASSIGNMENT,
				AssignmentDAO.INSTANCE);
		EntityTypeDAOFactory.INSTANCE.register(EntityType.MODULE,
                ModuleDAO.INSTANCE);
		EntityTypeDAOFactory.INSTANCE.register(EntityType.VIDEO,
				VideoDAO.INSTANCE);
		EntityTypeDAOFactory.INSTANCE.register(EntityType.FILE,
				FileDAO.INSTANCE);
		EntityTypeDAOFactory.INSTANCE.register(EntityType.DOCUMENT,
				DocumentDAO.INSTANCE);
		EntityTypeDAOFactory.INSTANCE.register(EntityType.REMARK,
				RemarkDAO.INSTANCE);
		EntityTypeDAOFactory.INSTANCE.register(EntityType.COMMENT,
				CommentDAO.INSTANCE);
		EntityTypeDAOFactory.INSTANCE.register(EntityType.CHALLENGE,
				ChallengeDAO.INSTANCE);

		EntityTypeDAOFactory.INSTANCE.register(EntityType.ORGANIZATION,
				OrganizationDAO.INSTANCE);
		EntityTypeDAOFactory.INSTANCE.register(EntityType.PROGRAM,
				OrgProgramDAO.INSTANCE);
		EntityTypeDAOFactory.INSTANCE.register(EntityType.CENTER,
				OrgCenterDAO.INSTANCE);
		EntityTypeDAOFactory.INSTANCE.register(EntityType.SECTION,
				OrgSectionDAO.INSTANCE);
		EntityTypeDAOFactory.INSTANCE.register(EntityType.USER,
				UserDAO.INSTANCE);

		EventRegistrar.INSTANCE.add(EventType.SHARE_ENTITY);
		EventRegistrar.INSTANCE.add(EventType.REMOVE_NEWS);
		EventRegistrar.INSTANCE.add(EventType.POST_REMARK);
		EventRegistrar.INSTANCE.add(EventType.SEND_EMAIL);
		EventRegistrar.INSTANCE.add(EventType.MESSAGE_DISTRIBUTE);

		// details registration for news

		EntityNewsDetailsFactory.INSTANCE.register(EntityType.FILE,
				FileNewsEntityDetails.class);
		EntityNewsDetailsFactory.INSTANCE.register(EntityType.VIDEO,
				VideoNewsEntityDetails.class);
		EntityNewsDetailsFactory.INSTANCE.register(EntityType.MODULE,
                ModuleNewsEntityDetails.class);
		EntityNewsDetailsFactory.INSTANCE.register(EntityType.DOCUMENT,
				DocumentNewsEntityDetails.class);

		EntityNewsDetailsFactory.INSTANCE.register(EntityType.QUESTION,
				QuestionNewsEntityDetails.class);
		EntityNewsDetailsFactory.INSTANCE.register(EntityType.SOLUTION,
				SolutionNewsDetails.class);
		EntityNewsDetailsFactory.INSTANCE.register(EntityType.DISCUSSION,
				DiscussionNewsEntityDetails.class);
		EntityNewsDetailsFactory.INSTANCE.register(EntityType.CHALLENGE,
				ChallengeNewsDetails.class);

		EntityNewsDetailsFactory.INSTANCE.register(EntityType.TEST,
				TestNewsEntityDetails.class);
		EntityNewsDetailsFactory.INSTANCE.register(EntityType.ASSIGNMENT,
				TestNewsEntityDetails.class);
		EntityNewsDetailsFactory.INSTANCE.register(EntityType.STATUSFEED,
				StatusFeedNewsEntityDetails.class);
		EntityNewsDetailsFactory.INSTANCE.register(EntityType.COMMENT,
				CommentNewsEntityDetails.class);
		EntityNewsDetailsFactory.INSTANCE.register(EntityType.REMARK,
				RemarkNewsEntityDetails.class);

		// Populator registrations for news
		EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.FILE,
				FileNewsPopulator.INSTANCE);
		EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.VIDEO,
				VideoNewsPopulator.INSTANCE);
		EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.MODULE,
                ModuleNewsPopulator.INSTANCE);
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
		EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.REMARK,
				RemarksNewsPopulator.INSTANCE);
		EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.COMMENT,
				CommentNewsPopulator.INSTANCE);
		EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.TEST,
				TestNewsPopulator.INSTANCE);
		EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.ASSIGNMENT,
				AssignmentNewsPopulator.INSTANCE);

		EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.USER,
				UserNewsPopulator.INSTANCE);
		EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.PROGRAM,
				ProgramNewsPopulator.INSTANCE);
		EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.CENTER,
				CenterNewsPopulator.INSTANCE);
		EntityDetailsPopulatorFactory.INSTANCE.register(EntityType.SECTION,
				SectionNewsPopulator.INSTANCE);
		

		NewsFeedSecurityVaildator.addPublicEvent(EventType.ADD_COMMENT);
		NewsFeedSecurityVaildator.addPublicEvent(EventType.ADD_SOLUTION);
		NewsFeedSecurityVaildator.addPublicEvent(EventType.ATTEMPT_ENTITY);
		NewsFeedSecurityVaildator.addPublicEvent(EventType.FOLLOW_ENTITY);
		NewsFeedSecurityVaildator.addPublicEvent(EventType.VOTE_ENTITY);
		NewsFeedSecurityVaildator.addPublicEvent(EventType.INDEX_CHALLENGE);
		NewsFeedSecurityVaildator.addPublicEvent(EventType.END_CHALLENGE);
	}
}
