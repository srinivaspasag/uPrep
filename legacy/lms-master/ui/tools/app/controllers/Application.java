package controllers;

import org.eclipse.jdt.internal.core.util.LRUCache;

import play.libs.Images;
import play.mvc.With;

@With(Security.class)
public class Application extends AbstractUIController {
protected static LRUCache cache = new LRUCache(100);
    public static void index() {
        render();
    }

    protected static Images.Captcha _getCaptcha(String id) {
        Images.Captcha captcha = Images.captcha();
        String code = captcha.getText("#6e6e6e");
        cache.put(id, code);
        return captcha;
    }

    public static void captcha(String id) {
        Images.Captcha captcha = _getCaptcha(id);
        renderBinary(captcha);
    }

}
