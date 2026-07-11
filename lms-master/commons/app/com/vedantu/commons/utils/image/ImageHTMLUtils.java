package com.vedantu.commons.utils.image;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.utils.ImageDisplayURLUtil;

public class ImageHTMLUtils {

    private static final ALogger LOGGER            = Logger.of(ImageHTMLUtils.class);
    private static final String  IMG_IDENTIFIER    = "v-uid";
    private static final String  IMG_SRC           = "src";
    private static final String  IMG_SRC_PERMANENT = "v-perm";
    private static final String  IMG_CLASS_NAME    = "vImageUrl";

    public static String addImageSrcUrl(EntityType entityType, String html) {

        if (StringUtils.isEmpty(html)) {
            return StringUtils.EMPTY;
        }
        Document doc = Jsoup.parseBodyFragment(html);
        Elements elements = doc.getElementsByAttribute(IMG_IDENTIFIER);
        Iterator<Element> it = elements.iterator();
        while (it.hasNext()) {
            Element es = it.next();
            String url = getImgUrl(entityType, es.attr(IMG_IDENTIFIER));
            if (StringUtils.isNotEmpty(url)) {
                es.attr(IMG_SRC, url);
                es.attr("class", IMG_CLASS_NAME);
                es.attr(IMG_SRC_PERMANENT, Boolean.toString(true));
            }
            LOGGER.info("html element url : " + url + " and element: " + es);
        }
        String htmlBody = doc.body().html();
        return htmlBody;
    }

    public static Set<String> getImageUUids( String html) {

        Set<String> uuids = new HashSet<String>();
        if (StringUtils.isEmpty(html)) {
            return new HashSet<String>();
        }
        Document doc = Jsoup.parseBodyFragment(html);
        Elements elements = doc.getElementsByAttribute(IMG_IDENTIFIER);
        Iterator<Element> it = elements.iterator();
        while (it.hasNext()) {
            Element es = it.next();
            if (es.attr(IMG_IDENTIFIER) != null) {
                uuids.add(es.attr(IMG_IDENTIFIER));
            }
        }

        return uuids;
    }

    public static String removeImageSrcUrl(String html, Set<String> uuids) {

        if (StringUtils.isEmpty(html)) {
            LOGGER.info(" HTML content is empty");
            return StringUtils.EMPTY;
        }

        Document doc = Jsoup.parseBodyFragment(html);
        Elements elements = doc.getElementsByAttribute(IMG_IDENTIFIER);
        Iterator<Element> it = elements.iterator();

        while (it.hasNext()) {
            Element es = it.next();
            String uuid = es.attr(IMG_IDENTIFIER);// v-uuid=type:uuid
            String isUrlPerm = es.attr(IMG_SRC_PERMANENT);
            if (StringUtils.isEmpty(isUrlPerm)) {
                uuids.add(uuid);
                es.attr(IMG_SRC_PERMANENT,"true");
                LOGGER.info(" uuid " + uuid);
            }
           
            es.attr(IMG_SRC, StringUtils.EMPTY);
        }
        return doc.body().html();
    }

    private static String getImgUrl(EntityType entityType, String dataUid) {

        LOGGER.info(" dataUid: " + dataUid);
        String url = ImageDisplayURLUtil.getEntityImageURL(entityType, dataUid);
        return url;
    }

    public static void main(String[] args) {

        String html = "sadfhsalf <img data-id='s:13213' src='http://afasdf' alt='ssk' /> there is something in between <img data-id='132134' src='http://afasdf' alt='ssk' /> sdfsaf";
        Document d = Jsoup.parseBodyFragment(html);
        System.out.println("document d : " + d);
        Elements e = d.getElementsByAttribute("data-id");
        Iterator<Element> it = e.iterator();
        while (it.hasNext()) {
            Element es = it.next();
            String data = es.attr("data-id");
            System.out.println(StringUtils.substringAfter(data, ":"));
            System.out.println(StringUtils.substringBefore(data, ":"));
            es.attr("data-id", "new value");
        }
        System.out.println(d.getElementsByAttribute("data-id"));
        System.out.println("new document d : " + d.body().html());
    }
}
