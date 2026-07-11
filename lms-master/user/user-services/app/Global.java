import play.Application;
import play.Logger;
import play.Logger.ALogger;

import com.vedantu.VedantuGlobalSettings;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.events.apis.EventRegistrar;
import com.vedantu.user.daos.UserDAO;

public class Global extends VedantuGlobalSettings {

	private static final ALogger	LOGGER	= Logger.of(Global.class);

	@Override
	public void beforeStart(Application app) {
		super.beforeStart(app);
		boolean result = UserDAO.INSTANCE._createDefaultUser();
		LOGGER.debug("default user creation result: " + result);
		EventRegistrar.INSTANCE.add(EventType.SEND_EMAIL);
		EventRegistrar.INSTANCE.add(EventType.SEND_INSTANT_EMAIL);
        
	}

}
