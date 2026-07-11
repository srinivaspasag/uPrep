import play.Application;
import play.Logger;
import play.Logger.ALogger;

import com.vedantu.VedantuGlobalSettings;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.events.apis.EventRegistrar;

public class Global extends VedantuGlobalSettings {

    private static final ALogger LOGGER = Logger.of(Global.class);

    @Override
    public void beforeStart(Application app) {

        super.beforeStart(app);

        EventRegistrar.INSTANCE.add(EventType.REMOVE_BOARD);
//        EventRegistrar.INSTANCE.add(EventType.SEND_INSTANT_EMAIL);

    }
}
