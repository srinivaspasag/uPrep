package util;

import java.util.Properties;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.libs.IO;
import play.vfs.VirtualFile;

public class MessagesPluginForJSFiles extends PlayPlugin {

    static Long                 lastLoading      = 0L;
    private static final String MESSAGES_JS_PATH = "conf/messages_js";

    @Override
    public void onApplicationStart() {

        MessagesForJSFiles.defaults = new Properties();
        for (VirtualFile module : Play.modules.values()) {
            VirtualFile messages = module.child(MESSAGES_JS_PATH);
            if (messages != null && messages.exists()) {
                MessagesForJSFiles.defaults.putAll(read(messages));
            }
        }
        VirtualFile appDM = Play.getVirtualFile(MESSAGES_JS_PATH);
        if (appDM != null && appDM.exists()) {
            MessagesForJSFiles.defaults.putAll(read(appDM));
        }
        for (String locale : Play.langs) {
            Properties properties = new Properties();
            for (VirtualFile module : Play.modules.values()) {
                VirtualFile messages = module.child(MESSAGES_JS_PATH + "." + locale);
                if (messages != null && messages.exists()) {
                    properties.putAll(read(messages));
                }
            }
            VirtualFile appM = Play.getVirtualFile(MESSAGES_JS_PATH + "." + locale);
            if (appM != null && appM.exists()) {
                properties.putAll(read(appM));
            } else {
                Logger.warn("MessagesForJSFiles file missing for locale %s", locale);
            }
            MessagesForJSFiles.locales.put(locale, properties);
        }
        lastLoading = System.currentTimeMillis();
    }

    static Properties read(VirtualFile vf) {

        if (vf != null) {
            return IO.readUtf8Properties(vf.inputstream());
        }
        return null;
    }

    @Override
    public void detectChange() {

        if (Play.getVirtualFile(MESSAGES_JS_PATH) != null
                && Play.getVirtualFile(MESSAGES_JS_PATH).lastModified() > lastLoading) {
            onApplicationStart();
            return;
        }
        for (VirtualFile module : Play.modules.values()) {
            if (module.child(MESSAGES_JS_PATH) != null && module.child(MESSAGES_JS_PATH).exists()
                    && module.child(MESSAGES_JS_PATH).lastModified() > lastLoading) {
                onApplicationStart();
                return;
            }
        }
        for (String locale : Play.langs) {
            if (Play.getVirtualFile(MESSAGES_JS_PATH + "." + locale) != null
                    && Play.getVirtualFile(MESSAGES_JS_PATH + "." + locale).lastModified() > lastLoading) {
                onApplicationStart();
                return;
            }
            for (VirtualFile module : Play.modules.values()) {
                if (module.child(MESSAGES_JS_PATH + "." + locale) != null
                        && module.child(MESSAGES_JS_PATH + "." + locale).exists()
                        && module.child(MESSAGES_JS_PATH + "." + locale).lastModified() > lastLoading) {
                    onApplicationStart();
                    return;
                }
            }
        }

    }

}
