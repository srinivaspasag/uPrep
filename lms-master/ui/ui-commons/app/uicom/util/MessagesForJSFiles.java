package uicom.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import play.Play;
import play.i18n.Lang;
import play.i18n.Messages;

public class MessagesForJSFiles extends Messages {

    static public Properties              defaults;

    static public Map<String, Properties> locales = new HashMap<String, Properties>();

    public static String get(Object key, Object... args) {

        return getMessage(Lang.get(), key, args);
    }

    public static String getMessage(String locale, Object key, Object... args) {

        // Check if there is a plugin that handles translation
        String message = Play.pluginCollection.getMessage(locale, key, args);

        if (message != null) {
            return message;
        }

        String value = null;
        if (key == null) {
            return "";
        }
        if (locales.containsKey(locale)) {
            value = locales.get(locale).getProperty(key.toString());
        }
        if (value == null) {
            value = defaults.getProperty(key.toString());
        }
        if (value == null) {
            value = key.toString();
        }

        return formatString(value, args);
    }

    public static Properties all(String locale) {

        if (locale == null || "".equals(locale))
            return defaults;
        return locales.get(locale);
    }
}
